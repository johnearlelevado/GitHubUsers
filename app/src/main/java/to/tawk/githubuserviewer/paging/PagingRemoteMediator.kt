package to.tawk.githubuserviewer.paging

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.*
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import to.tawk.githubuserviewer.api.users.service.UsersService
import to.tawk.githubuserviewer.room.AppDatabase
import to.tawk.githubuserviewer.room.dao.UserDao
import to.tawk.githubuserviewer.room.entities.Details
import to.tawk.githubuserviewer.room.entities.User

@OptIn(ExperimentalPagingApi::class)
class PagingRemoteMediator(
    private val db: AppDatabase,
    private val usersService: UsersService
) : RemoteMediator<Int, User>() {

    private val userDao: UserDao = db.userDao()

    override suspend fun initialize(): InitializeAction {
        // Require that remote REFRESH is launched on initial load and succeeds before launching
        // remote PREPEND / APPEND.
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, User>
    ): MediatorResult {
        Log.d("PageKeyedRemoteMediator","load()  load type = $loadType")
        try {
            // Get the closest item from PagingState that we want to load data around.
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

            val items = usersService.getUsers(
                since = loadKey,
                per_page = when (loadType) {
                    REFRESH -> state.config.initialLoadSize
                    else -> state.config.pageSize
                }
            )

            if (loadType ==  REFRESH) {
                userDao.deleteUsers()
            }

            db.withTransaction {
                userDao.insertAllUsers(items)
            }

            return MediatorResult.Success(endOfPaginationReached = items.isEmpty())
        } catch (e: Throwable) {
            return MediatorResult.Error(e)
        }
    }

    companion object {
        const val STARTING_INDEX = 0
    }
}