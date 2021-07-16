package to.tawk.githubuserviewer.viewmodels

import android.text.TextUtils
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import to.tawk.githubuserviewer.repository.Repository
import to.tawk.githubuserviewer.room.entities.User
import javax.inject.Inject

@HiltViewModel
open class UsersViewModel @Inject constructor(
    val repository: Repository
) : BaseViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun posts(search:String?) = flowOf(
        if (TextUtils.isEmpty(search))
            repository.getUsersPaginated(30)
        else
            repository.getUsersPaginated(search,30).map { pagingData ->
                pagingData.map { userDetails -> userDetails.user }
            }
    ).flattenMerge(2)

}