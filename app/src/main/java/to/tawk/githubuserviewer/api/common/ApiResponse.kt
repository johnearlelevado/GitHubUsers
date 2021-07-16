package to.tawk.githubuserviewer.api.common

import android.os.Bundle

class ApiResponse<T> {

    var mResponse : T?
    var mError : Int?
    var mStatus : Status
    var mThrowable : Throwable?
    var mExtra : Bundle?

    constructor(status: Status,
                response:T? = null,
                error:Int? = null,
                throwable: Throwable? = null,
                extra: Bundle? = null ){
        mResponse = response
        mError = error
        mStatus = status
        mThrowable = throwable
        mExtra = extra
    }

    companion object {
        fun <T>loading() : ApiResponse<T> {
            return ApiResponse(status = Status.LOADING)
        }

        fun <T>success(response:T? = null,extra: Bundle? = null) : ApiResponse<T> {
            return ApiResponse(
                status = Status.SUCCESS,
                response = response,
                extra = extra
            )
        }

        fun  <T>error(error:Int? = null ,extra: Bundle? = null) : ApiResponse<T> {
            return ApiResponse(
                status = Status.ERROR,
                error = error,
                extra = extra
            )
        }

        fun <T>fail(throwable: Throwable) : ApiResponse<T> {
            return ApiResponse(
                status = Status.FAIL,
                throwable = throwable
            )
        }
    }
}


