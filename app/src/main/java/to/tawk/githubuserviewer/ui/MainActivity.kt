package to.tawk.githubuserviewer.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import to.tawk.githubuserviewer.databinding.ActivityMainBinding
import to.tawk.githubuserviewer.paging.asMergedLoadStates
import to.tawk.githubuserviewer.room.AppDatabase
import to.tawk.githubuserviewer.ui.adapters.UserListLoadStateAdapter
import to.tawk.githubuserviewer.ui.adapters.UserListAdapter
import to.tawk.githubuserviewer.viewmodels.UsersViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    lateinit var binding: ActivityMainBinding
    private val model: UsersViewModel by viewModels()
    lateinit var adapter : UserListAdapter
    @Inject
    lateinit var appDatabase: AppDatabase

    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        initAdapter()
        initSwipeToRefresh()
        initSearch()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UserDetailsActivity.REQUEST_CODE && resultCode == RESULT_OK) {
            adapter.notifyDataSetChanged()
        }
    }

    @InternalCoroutinesApi
    private fun initAdapter() {
        adapter = UserListAdapter(this, appDatabase.userDetailsDao())
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
            model.posts("").collectLatest {
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
        binding.inputLayout.setOnClickListener {
            binding.input.requestFocus()
        }
        binding.input.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                updatedSubredditFromInput(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (TextUtils.isEmpty(newText)) {
                    updatedSubredditFromInput(null)
                }
                return true
            }
        })
    }

    private fun updatedSubredditFromInput(query:String?) {
        lifecycleScope.launchWhenCreated {
            model.posts(query).collectLatest {
                adapter.submitData(it)
            }
        }
    }




}