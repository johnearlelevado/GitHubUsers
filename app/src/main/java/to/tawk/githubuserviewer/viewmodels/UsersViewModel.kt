package to.tawk.githubuserviewer.viewmodels

import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import to.tawk.githubuserviewer.repository.Repository
import javax.inject.Inject

@HiltViewModel
open class UsersViewModel @Inject constructor(
    val repository: Repository
) : BaseViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun getUsers(search:String?) = flowOf(
        if (search.isNullOrEmpty())
            repository.getUsersFromDBAndNetwork(30)
        else
            repository.getUsersWithSearchFromDBOnly(search,30).map { pagingData ->
                pagingData.map { userDetails -> userDetails.user }
            }
    ).flattenMerge(2)

    fun getAppDB() = repository.getAppDB()

}