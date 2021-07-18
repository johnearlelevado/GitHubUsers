package to.tawk.githubusers.room.dao

import androidx.paging.PagingSource
import androidx.room.*
import to.tawk.githubusers.room.entities.User
import to.tawk.githubusers.room.entities.UserDetails

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    @JvmSuppressWildcards
    suspend fun insertAllUsers(list: List<User>)

    @Query("SELECT * FROM user ORDER BY id ASC")
    fun getUsersPaginated(): PagingSource<Int, User>

    @Query("SELECT * FROM user WHERE login LIKE :search ORDER BY id ASC")
    fun getUsersSearchPaginated(search:String): PagingSource<Int, User>

    @Query("SELECT * FROM user INNER JOIN details " +
            "ON user.login = details.login_details " +
            "WHERE (user.login LIKE :search OR details.note_details LIKE :search) " +
            "ORDER BY id ASC")
    fun getUserDetailsSearchPaginated(search:String): PagingSource<Int, UserDetails>

    @Query("SELECT MAX(id) + 1 FROM user")
    suspend fun getUsersNextIndex(): Int?

    @Query("DELETE FROM user")
    suspend fun deleteUsers()
}