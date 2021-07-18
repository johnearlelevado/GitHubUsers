package to.tawk.githubuserviewer.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import to.tawk.githubuserviewer.room.AppDatabase
import to.tawk.githubuserviewer.room.entities.User
import to.tawk.githubuserviewer.room.entities.Details
import to.tawk.githubuserviewer.room.entities.UserDetails

/**
 * Common interface shared by the different repository implementations.
 * Note: this only exists for sample purposes - typically an app would implement a repo once, either
 * network+db, or network-only
 */
interface Repository {
    fun getUsersWithSearchFromDBOnly(query: String?, pageSize: Int): Flow<PagingData<UserDetails>>

    fun getUsersFromDBAndNetwork(pageSize: Int): Flow<PagingData<User>>

    fun getUserDetails(username:String): Details

    fun getAppDB(): AppDatabase
}