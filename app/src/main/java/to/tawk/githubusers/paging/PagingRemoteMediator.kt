package to.tawk.githubusers.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.*
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import to.tawk.githubusers.api.users.service.UsersService
import to.tawk.githubusers.room.AppDatabase
import to.tawk.githubusers.room.dao.UserDao
import to.tawk.githubusers.room.entities.Details
import to.tawk.githubusers.room.entities.User
import java.lang.Exception

@OptIn(ExperimentalPagingApi::class)
class PagingRemoteMediator(
    private val db: AppDatabase,
    private val usersService: UsersService
) : RemoteMediator<Int, User>() {

    private val userDao: UserDao = db.userDao()

    override suspend fun initialize(): InitializeAction {
        try {
            val hasDownloadedAlready = (db.userDao().getUsersNextIndex() ?: 1) > 1
            return if (hasDownloadedAlready) InitializeAction.SKIP_INITIAL_REFRESH else InitializeAction.LAUNCH_INITIAL_REFRESH
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, User>
    ): MediatorResult {
        try {
            val loadKey = when (loadType) {
                REFRESH -> STARTING_INDEX
                PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                APPEND -> {
                    val nextPageKey = db.withTransaction {
                        userDao.getUsersNextIndex() ?: -1
                    }
                    if (nextPageKey == -1) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }
                    nextPageKey
                }
            }

            // fetch users from API when there are no items to display or end of page is reached
            val items = usersService.getUsers(
                since = loadKey,
                per_page = when (loadType) {
                    REFRESH -> state.config.initialLoadSize
                    else -> state.config.pageSize
                }
            ).mapIndexed { index, user -> user?.apply { position = index }   }


            if (loadType ==  REFRESH) {
               userDao.deleteUsers()
            }

            userDao.insertAllUsers(items)
            items.forEach { u->
                db.userDetailsDao().insertUserDetailIgnore( Details(
                    login = u.login,
                    id = u.id,
                    avatar_url = u.avatar_url
                ))
            }

            return MediatorResult.Success(endOfPaginationReached = items.isEmpty())
        } catch (e: Throwable) {
            e.printStackTrace()
            return MediatorResult.Error(e)
        }
    }

    companion object {
        const val STARTING_INDEX = 0
    }
}