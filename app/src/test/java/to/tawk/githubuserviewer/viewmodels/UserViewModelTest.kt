package to.tawk.githubuserviewer.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.*
import androidx.recyclerview.widget.ListUpdateCallback
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat

import to.tawk.githubuserviewer.api.common.scheduler.SchedulerProvider
import to.tawk.githubuserviewer.repository.RepositoryImpl
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import to.tawk.githubuserviewer.api.users.service.UsersService
import to.tawk.githubuserviewer.mock.UserDetailsMock
import to.tawk.githubuserviewer.paging.PagingRemoteMediator
import to.tawk.githubuserviewer.room.AppDatabase
import to.tawk.githubuserviewer.room.dao.DetailsDao
import to.tawk.githubuserviewer.room.dao.UserDao
import to.tawk.githubuserviewer.room.entities.Details
import to.tawk.githubuserviewer.room.entities.User
import to.tawk.githubuserviewer.room.entities.UserDetails
import to.tawk.githubuserviewer.ui.adapters.UserListAdapter
import kotlin.math.log


@RunWith(MockitoJUnitRunner::class)
class UserViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    companion object {
        @ClassRule
        @JvmField
        val schedulers =
            RxImmediateSchedulerRule()
    }

    @Mock
    lateinit var appDatabase: AppDatabase

    private lateinit var viewModel: UsersViewModel

    lateinit var list: List<User>
    lateinit var schedulerProviderMock : SchedulerProvider

    @Before
    fun setup() {

        list = listOf(UserDetailsMock.getUser())

        schedulerProviderMock = mock<SchedulerProvider>(){}

        Dispatchers.setMain(testDispatcher)
    }

    private fun setupSuccessResponse() {
        val usersService = mock<UsersService> {

        }
        val repository = RepositoryImpl(db = appDatabase, usersService = usersService)
        viewModel = UsersViewModel(
            repository = repository
        )
    }


    @ExperimentalPagingApi
    @Test
    fun `fetch users successfully`() = runBlocking {
        // Add mock results for the API to return.

        val usersService = mock<UsersService> {
            on { runBlocking { getUsers(since = 0,per_page = 30) }  } doReturn list
        }

        val userDao = mock<UserDao> {
            on { runBlocking { deleteUsers() }  } doReturn Unit
        }
        val detailsDao = mock<DetailsDao> {}
        val appDatabase = mock<AppDatabase> {
            on { userDao()  } doReturn userDao
            on { userDetailsDao() } doReturn detailsDao
        }

        val remoteMediator = PagingRemoteMediator(
            appDatabase,
            usersService
        )
        val pagingState = PagingState<Int, User>(
            listOf(),
            null,
            PagingConfig(10),
            10
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)
        assertThat ( result is RemoteMediator.MediatorResult.Success ).isTrue()
    }


    @Test
    fun `app database should not be null`() {
        setupSuccessResponse()
        assertThat(viewModel.getAppDB()).isNotNull()
    }

    private val testDispatcher = TestCoroutineDispatcher()

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test user fetching if successful`() = runBlockingTest(testDispatcher) {

        val list = listOf(UserDetailsMock.getUser())

        val usersService = mock<UsersService> {}

        val userDao = UserDaoFake(list)

        val appDatabase = mock<AppDatabase> {
            on { userDao()  } doReturn userDao
        }

        val repository = RepositoryImpl(db = appDatabase, usersService = usersService)
        val viewModel = UsersViewModel(
            repository = repository
        )

        val differ = AsyncPagingDataDiffer(
            diffCallback = UserListAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            mainDispatcher = testDispatcher,
            workerDispatcher = testDispatcher,
        )

        // submitData allows differ to receive data from PagingData, but suspends until
        // invalidation, so we must launch this in a separate job.
        val job = launch {
            viewModel.getUsers("fake-login").collectLatest { pagingData ->
                differ.submitData(pagingData)
            }
        }

        // Wait for initial load to finish.
        advanceUntilIdle()

        assertThat(!differ.snapshot().isEmpty()).isTrue()

        assertThat(differ.snapshot()).containsExactly(
            list[0]
        )

        // runBlockingTest checks for leaking jobs, so we have to cancel the one we started.
        job.cancel()
    }

    private val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }

    class UserDaoFake(val users: List<User>) : UserDao {
        override suspend fun insertAllUsers(list: List<User>) {

        }

        override fun getUsersPaginated(): PagingSource<Int, User> {
            TODO("Not yet implemented")
        }

        override fun getUsersSearchPaginated(search: String): PagingSource<Int, User> {
            return object : PagingSource<Int, User>() {
                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
                    return LoadResult.Page(
                        data = users,
                        prevKey = null,
                        nextKey = null,
                    )
                }

                // Ignored in test.
                override fun getRefreshKey(state: PagingState<Int, User>): Int? = null
            }
        }

        override fun getUserDetailsSearchPaginated(search: String): PagingSource<Int, UserDetails> {
            return object : PagingSource<Int, UserDetails>() {
                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserDetails> {
                    return LoadResult.Page(
                        data = listOf(UserDetails(user = UserDetailsMock.getUser() ,details = UserDetailsMock.getDetails())),
                        prevKey = null,
                        nextKey = null,
                    )
                }

                // Ignored in test.
                override fun getRefreshKey(state: PagingState<Int, UserDetails>): Int? = null
            }
        }

        override suspend fun getUsersNextIndex(): Int? { return 1 }

        override suspend fun deleteUsers() { }

    }

}
