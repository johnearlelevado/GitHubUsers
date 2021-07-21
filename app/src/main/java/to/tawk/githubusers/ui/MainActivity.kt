package to.tawk.githubusers.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import io.supercharge.shimmerlayout.ShimmerLayout
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

    lateinit var binding: ActivityMainBinding
    private val viewModel: UsersViewModel by viewModels()
    lateinit var adapter : UserListAdapter

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

    private fun initUserDetailCallback() : ActivityResultLauncher<Intent> {
        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                fetchUsers(null)
                adapter.notifyDataSetChanged()
            }
        }
        return startForResult
    }

    private fun initNetworkConnectionStatusHandler() {
        NetworkStatusUtil(this) {
            adapter.retry()
        }.apply {
            build(binding.tvNetworkStatusBar)
        }
    }



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

    @InternalCoroutinesApi
    private fun initAdapter(launcher: ActivityResultLauncher<Intent>) {
        adapter = UserListAdapter(this, viewModel,launcher)
        binding.list.layoutManager?.onSaveInstanceState()
        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = UserListLoadStateAdapter(adapter),
            footer = UserListLoadStateAdapter(adapter)
        )

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { loadStates ->
                binding.swipeRefresh.isRefreshing = loadStates.mediator?.refresh is LoadState.Loading
            }
        }

        lifecycleScope.launchWhenCreated {
            showSkeleton(true)
            viewModel.getUsers("").collectLatest {
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

    private fun initSwipeToRefresh() {
        binding.swipeRefresh.setOnRefreshListener { adapter.refresh() }
    }

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

    private fun fetchUsers(query:String?) {
        lifecycleScope.launchWhenCreated {
            viewModel.getUsers(query).collectLatest {
                adapter.submitData(it)
            }
        }
    }
}