package to.tawk.githubuserviewer.viewmodels

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

open class BaseViewModel : ViewModel() {
    protected var compositeDisposable = CompositeDisposable()

    // To handle all sub class viewmodels' composite disposable clearing
    override fun onCleared() {
        compositeDisposable.clear()
    }
}