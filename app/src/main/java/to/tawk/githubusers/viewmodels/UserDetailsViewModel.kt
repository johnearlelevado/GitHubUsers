package to.tawk.githubusers.viewmodels

import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import to.tawk.githubusers.api.common.ApiResponse
import to.tawk.githubusers.api.common.scheduler.SchedulerProvider
import to.tawk.githubusers.api.users.service.UsersService
import to.tawk.githubusers.repository.Repository
import to.tawk.githubusers.room.entities.Details
import javax.inject.Inject

@HiltViewModel
open class UserDetailsViewModel @Inject constructor(
    private val usersService: UsersService,
    private val schedulerProvider: SchedulerProvider,
    private val repository: Repository
) : BaseViewModel() {

    val userDetailsLiveData = MutableLiveData<ApiResponse<Details>>()

    init {
        compositeDisposable = CompositeDisposable()
    }


    fun getUserDetails(username:String){
        val service = usersService.getUserDetails(username = username)
        compositeDisposable.add(service
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {userDetailsLiveData.value = ApiResponse.loading() }
            .subscribe({ response ->
                if (response.isSuccessful) {
                    userDetailsLiveData.value = ApiResponse.success(response = response.body())
                } else  {
                    try {
                        userDetailsLiveData.value = ApiResponse.error(response.code())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        userDetailsLiveData.value = ApiResponse.fail(e)
                    }
                }
            }, { t ->
                t.printStackTrace()
                userDetailsLiveData.value = ApiResponse.fail(t)
            }))
    }

    fun getAppDB() = repository.getAppDB()

}