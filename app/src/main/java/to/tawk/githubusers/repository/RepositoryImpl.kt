package to.tawk.githubusers.repository;


import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import to.tawk.githubusers.api.users.service.UsersService
import to.tawk.githubusers.room.AppDatabase
import to.tawk.githubusers.room.entities.User
import to.tawk.githubusers.paging.PagingRemoteMediator
import to.tawk.githubusers.room.entities.Details
import to.tawk.githubusers.room.entities.UserDetails

/**
 * Repository implementation that uses a database backed [androidx.paging.PagingSource] and
 * [androidx.paging.RemoteMediator] to load pages from network when there are no more items cached
 * in the database to load.
 */
class RepositoryImpl(val db: AppDatabase, val usersService: UsersService) : Repository {

    /**
     * Fetches UserDetails from DB only
     * Typically used when searching
     * @param search is the keyword to be searched in User's login/username and Detail's notes
     * @param pageSize is the number of items per page
     * */
    @OptIn(ExperimentalPagingApi::class)
    override fun getUsersWithSearchFromDBOnly(search: String?, pageSize: Int): Flow<PagingData<UserDetails>> = Pager(
        config = PagingConfig(pageSize)
    ) {
        // appending '%' so we can allow other characters to be before and after the query string
        val dbQuery = "%${search?.replace(' ', '%')}%"
        db.userDao().getUserDetailsSearchPaginated(dbQuery)
    }.flow

    /**
     * Fetches UserDetails from DB+Network
     * Typically when initializing the list and when not searching
     * @param pageSize is the number of items per page
     * */
    @OptIn(ExperimentalPagingApi::class)
    override fun getUsersFromDBAndNetwork(pageSize: Int): Flow<PagingData<User>> = Pager(
        config = PagingConfig(pageSize),
        remoteMediator = PagingRemoteMediator(db, usersService)
    ) {
        db.userDao().getUsersPaginated()
    }.flow

    /**
     * Fetches the Detail based on the given
     * @param username
     * */
    override fun getUserDetails(username: String): Details
        = db.userDetailsDao().getUsersDetail(login = username)

    override fun getAppDB(): AppDatabase = db
}
