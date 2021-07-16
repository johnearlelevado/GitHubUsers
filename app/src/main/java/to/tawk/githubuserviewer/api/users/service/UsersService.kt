package to.tawk.githubuserviewer.api.users.service

import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import to.tawk.githubuserviewer.room.entities.User
import to.tawk.githubuserviewer.room.entities.Details

interface UsersService {
    @GET("/users")
    suspend fun getUsers(
        @Query("since") since: Int = 0,
        @Query("per_page") per_page: Int = 30
    ): List<User>

    @GET("/users/{username}")
    fun getUserDetails(
        @Path("username") username: String
    ): Observable<Response<Details>>
}

