package to.tawk.githubusers.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import to.tawk.githubusers.api.common.ApiResponse
import to.tawk.githubusers.api.common.Status
import to.tawk.githubusers.api.common.scheduler.SchedulerProvider
import to.tawk.githubusers.repository.RepositoryImpl
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response
import to.tawk.githubusers.api.users.service.UsersService
import to.tawk.githubusers.room.AppDatabase
import to.tawk.githubusers.room.entities.Details


@RunWith(MockitoJUnitRunner::class)
class UserDetailsViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    companion object {
        @ClassRule
        @JvmField
        val schedulers =
            RxImmediateSchedulerRule()
    }

    @Mock
    lateinit var observerObject: Observer<ApiResponse<Details>>
    @Mock
    lateinit var appDatabase: AppDatabase

    private lateinit var viewModel: UserDetailsViewModel

    lateinit var details: Details
    lateinit var schedulerProviderMock : SchedulerProvider

    @Before
    fun setup() {

        details = Details(login = "fake-login",id = 0,node_id = "fake-node-id")

        schedulerProviderMock = mock<SchedulerProvider>(){
            on { io() } doReturn Schedulers.io()
            on { ui() } doReturn AndroidSchedulers.mainThread()
        }
    }

    private fun setupSuccessResponse() {
        val response: Response<Details> = Response.success(details)
        val usersService = mock<UsersService> {
            on { getUserDetails(username = "fake-login") } doReturn Observable.just(
                response
            )
        }
        val repository = RepositoryImpl(db = appDatabase, usersService = usersService)
        viewModel = UserDetailsViewModel(
            usersService = usersService,
            schedulerProvider = schedulerProviderMock,
            repository = repository
        )
    }

    private fun setupFailedResponse() {
        val response: Response<Details> = Response.error(404,
            ResponseBody.create(
                MediaType.parse("application/json"),
            "{}"))
        val usersService = mock<UsersService> {
            on { getUserDetails(username = "fake-login") } doReturn Observable.just(
                response
            )
        }
        val repository = RepositoryImpl(db = appDatabase, usersService = usersService)
        viewModel = UserDetailsViewModel(
            usersService = usersService,
            schedulerProvider = schedulerProviderMock,
            repository = repository
        )
    }

    @Test
    fun `fetch user details successfully`() {
        setupSuccessResponse()
        viewModel.getUserDetails(username = "fake-login")
        viewModel.userDetailsLiveData.observeForever(observerObject)
        val value = viewModel.userDetailsLiveData.value
        assertThat(value?.mStatus).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `fetch user details failed`() {
        setupFailedResponse()
        viewModel.getUserDetails(username = "fake-login")
        viewModel.userDetailsLiveData.observeForever(observerObject)
        val value = viewModel.userDetailsLiveData.value
        assertThat(value?.mStatus).isEqualTo(Status.ERROR)
    }

    @Test
    fun `app database should not be null`() {
        setupSuccessResponse()
        assertThat(viewModel.getAppDB()).isNotNull()
    }


}
