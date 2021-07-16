package to.tawk.githubuserviewer.repository;


import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import to.tawk.githubuserviewer.api.users.service.UsersService
import to.tawk.githubuserviewer.room.AppDatabase
import to.tawk.githubuserviewer.room.entities.User
import to.tawk.githubuserviewer.paging.PagingRemoteMediator
import to.tawk.githubuserviewer.room.entities.Details
import to.tawk.githubuserviewer.room.entities.UserDetails

/**
 * Repository implementation that uses a database backed [androidx.paging.PagingSource] and
 * [androidx.paging.RemoteMediator] to load pages from network when there are no more items cached
 * in the database to load.
 */
class RepositoryImpl(val db: AppDatabase, val usersService: UsersService) : Repository {



    @OptIn(ExperimentalPagingApi::class)
    override fun getUsersPaginated(search: String?, pageSize: Int): Flow<PagingData<UserDetails>> = Pager(
        config = PagingConfig(pageSize)
    ) {
        // appending '%' so we can allow other characters to be before and after the query string
        val dbQuery = "%${search?.replace(' ', '%')}%"
        db.userDao().getUserDetailsSearchPaginated(dbQuery)
    }.flow

    @OptIn(ExperimentalPagingApi::class)
    override fun getUsersPaginated(pageSize: Int): Flow<PagingData<User>> = Pager(
        config = PagingConfig(pageSize),
        remoteMediator = PagingRemoteMediator(db, usersService)
    ) {
        db.userDao().getUsersPaginated()
    }.flow

    override fun getUserDetails(username: String): Details
        = db.userDetailsDao().getUsersDetail(login = username)
}
