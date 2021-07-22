package to.tawk.githubusers.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import io.supercharge.shimmerlayout.ShimmerLayout
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import to.tawk.githubusers.R
import to.tawk.githubusers.databinding.ActivityMainBinding
import to.tawk.githubusers.paging.MergedLoadStates.asMergedLoadStates
import to.tawk.githubusers.ui.adapters.UserListAdapter
import to.tawk.githubusers.ui.adapters.UserListLoadStateAdapter
import to.tawk.githubusers.util.NetworkStatusUtil
import to.tawk.githubusers.util.SkeletonUtils
import to.tawk.githubusers.viewmodels.UsersViewModel


@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: UsersViewModel by viewModels()
    private lateinit var adapter : UserListAdapter
    private lateinit var networkStatusUtil: NetworkStatusUtil

    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val i = initUserDetailCallback()
        initAdapter(i)
        initSwipeToRefresh()
        initSearch()
        initNetworkConnectionStatusHandler()

    }

    /**
     * Setup activity callback to refresh list
     * */
    private fun initUserDetailCallback(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                fetchUsers(null)
                adapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * Handle no network scenario.
     * Show an 'offline' message when the network is not available and
     * retrieve retry the API request when the network is available
     * */
    private fun initNetworkConnectionStatusHandler() {
        networkStatusUtil = NetworkStatusUtil(this) {
            adapter.retry()
        }.apply {
            build(binding.tvNetworkStatusBar)
        }
    }


    /**
     * show the skeleton loading placeholder while loading data
     * to the user list
     * */
    private fun showSkeleton(show: Boolean) {

        val skeletonLayout = findViewById<LinearLayout>(R.id.skeletonLayout)
        val shimmer = findViewById<ShimmerLayout>(R.id.shimmerSkeleton)

        if (show) {
            skeletonLayout.removeAllViews()
            val skeletonRows = SkeletonUtils.getSkeletonRowCount(this)
            for (i in 0..skeletonRows) {
                val rowLayout = layoutInflater.inflate(R.layout.skeleton_row_layout, null) as ViewGroup
                skeletonLayout.addView(rowLayout)
            }
            shimmer.visibility = View.VISIBLE
            shimmer.startShimmerAnimation()
            skeletonLayout.visibility = View.VISIBLE
            skeletonLayout.bringToFront()
        } else {
            shimmer.stopShimmerAnimation()
            shimmer.visibility = View.GONE
            skeletonLayout.removeAllViews()
            skeletonLayout.visibility = View.GONE
        }
    }

    /**
     * Saving keyword and recyclerViewState data in the viewModel when configuration changes.
     * Another alternative is to save via
     * @param outstate
     * */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.searchKeyword = binding.input.query.toString()
        viewModel.recyclerViewState = binding.list.layoutManager?.onSaveInstanceState()
    }

    @InternalCoroutinesApi
    private fun initAdapter(launcher: ActivityResultLauncher<Intent>) {

        /**
         * if recyclerViewState is not null, restore previous state of the
         * recyclerview before the activity was destroyed
         * */
        if (viewModel.recyclerViewState != null){
            binding.list.layoutManager?.onRestoreInstanceState(viewModel.recyclerViewState)
        }

        adapter = UserListAdapter(this, viewModel,launcher)
        binding.list.layoutManager?.onSaveInstanceState()
        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = UserListLoadStateAdapter(adapter),
            footer = UserListLoadStateAdapter(adapter)
        )

        // sync the SwipeRefreshLayout refresh status and the list loading status
        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { loadStates ->
                binding.swipeRefresh.isRefreshing = loadStates.mediator?.refresh is LoadState.Loading
            }
        }

        // fetch initial data for the list from DB and/or Network
        lifecycleScope.launchWhenCreated {
            // show loading skeleton as placeholder while the fetching process is not yet finished
            showSkeleton(true)
            viewModel.getUsers(viewModel.searchKeyword).collectLatest {
                delay(500)
                showSkeleton(false)
                adapter.submitData(it)
            }
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow
                // Use a state-machine to track LoadStates such that we only transition to
                // NotLoading from a RemoteMediator load if it was also presented to UI.
                .asMergedLoadStates()
                // Only emit when REFRESH changes, as we only want to react on loads replacing the
                // list.
                .distinctUntilChangedBy { it.refresh }
                // Only react to cases where REFRESH completes i.e., NotLoading.
                .filter { it.refresh is LoadState.NotLoading }
                // Scroll to top is synchronous with UI updates, even if remote load was triggered.
                .collect { binding.list.scrollToPosition(0) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkStatusUtil.br)
    }

    private fun initSwipeToRefresh() {
        binding.swipeRefresh.setOnRefreshListener { adapter.refresh() }
    }

    /**
     * setup searchview to fetch users when a search keyword exists
     * (filter users with a keyword based on username/login and notes) or
     * the searchview has been emptied (show all users)
     * */
    private fun initSearch() {
        binding.input.requestFocus()
        binding.input.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                fetchUsers(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (TextUtils.isEmpty(newText)) {
                    fetchUsers(null)
                }
                return true
            }
        })
    }

    // fetch users from the viewmodel and populate the list
    private fun fetchUsers(query:String?) {
        lifecycleScope.launchWhenCreated {
            viewModel.getUsers(query).collectLatest {
                adapter.submitData(it)
            }
        }
    }
}