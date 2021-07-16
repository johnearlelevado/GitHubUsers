package to.tawk.githubuserviewer.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import to.tawk.githubuserviewer.api.common.ApiResponse
import to.tawk.githubuserviewer.api.common.Status
import to.tawk.githubuserviewer.api.common.scheduler.SchedulerProvider
import to.tawk.githubuserviewer.repository.RepositoryImpl
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


@RunWith(MockitoJUnitRunner::class)
class CrimesViewModelTest {

    /*@get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    companion object {
        @ClassRule
        @JvmField
        val schedulers =
            RxImmediateSchedulerRule()
    }

    @Mock
    lateinit var observerObject: Observer<ApiResponse<List<CrimeModel.CrimeData>>>

    private lateinit var viewModel: CrimesViewModel

    lateinit var list : ArrayList<CrimeModel.CrimeData>
    lateinit var schedulerProviderMock : SchedulerProvider

    @Before
    fun setup() {
        list = ArrayList()
        list.add(
            CrimeModel.CrimeData(
                category = "fake-category",context = "fake-context",id = 0,location = CrimeModel.Location(
                    latitude = 0.0, longitude = 0.0, street = CrimeModel.Street(id=0,name = "fake-street-name")
                ),location_subtype = "fake-location-subtype",location_type = "fake-location-type",month = "fake-month",
                persistent_id = "fake-id"
            ))

        schedulerProviderMock = mock<SchedulerProvider>(){
            on { io() } doReturn Schedulers.io()
            on { ui() } doReturn AndroidSchedulers.mainThread()
        }
    }

    private fun setupSuccessResponse() {
        val response: Response<List<CrimeModel.CrimeData>> = Response.success(list)
        val crimeDataServiceMock = mock<CrimeDataService> {
            on { getCrimesByCustomArea("1.0,1.0:1.0,1.0", "2020-10") } doReturn Observable.just(
                response
            )
            on { getCrimesByLocation("2020-10", 1.0, 1.0) } doReturn Observable.just(response)
        }
        val repository = RepositoryImpl(crimeDataServiceMock, schedulerProviderMock)
        viewModel = CrimesViewModel(repository)
    }

    private fun setupFailedResponse() {
        val response: Response<List<CrimeModel.CrimeData>> = Response.error(404,
            ResponseBody.create(
                MediaType.parse("application/json"),
            "{}"))
        val crimeDataServiceMock = mock<CrimeDataService> {
            on { getCrimesByCustomArea("1.0,1.0:1.0,1.0", "2020-10") } doReturn Observable.just(
                response
            )
            on { getCrimesByLocation("2020-10", 1.0, 1.0) } doReturn Observable.just(response)
        }
        val repository = RepositoryImpl(crimeDataServiceMock, schedulerProviderMock)
        viewModel = CrimesViewModel(repository)
    }

    @Test
    fun `fetch crime data by area successfully`() {
        setupSuccessResponse()

        viewModel.getCrimesByArea("1.0,1.0:1.0,1.0","2020-10")
        viewModel.crimesByAreaObservable.observeForever(observerObject)
        val value = viewModel.crimesByAreaObservable.value
        assertThat(value?.mStatus).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `fetch crime data by area failed`() {
        setupFailedResponse()

        viewModel.getCrimesByArea("1.0,1.0:1.0,1.0","2020-10")
        viewModel.crimesByAreaObservable.observeForever(observerObject)
        val value = viewModel.crimesByAreaObservable.value
        assertThat(value?.mStatus).isEqualTo(Status.ERROR)
    }



    @Test
    fun `fetch crime data by specific location successfully`() {
        setupSuccessResponse()

        viewModel.getCrimesBySpecificLocation("2020-10",1.0,1.0)
        viewModel.crimesBySpecificLocationObservable.observeForever(observerObject)
        val value = viewModel.crimesBySpecificLocationObservable.value
        assertThat(value?.mStatus).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `fetch crime data by specific location failed`() {
        setupFailedResponse()

        viewModel.getCrimesBySpecificLocation("2020-10",1.0,1.0)
        viewModel.crimesBySpecificLocationObservable.observeForever(observerObject)
        val value = viewModel.crimesBySpecificLocationObservable.value
        assertThat(value?.mStatus).isEqualTo(Status.ERROR)
    }*/
}
