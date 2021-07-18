package to.tawk.githubuserviewer.room.dao

import androidx.room.*
import to.tawk.githubuserviewer.room.entities.Details

@Dao
interface DetailsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    suspend fun insertUserDetail(details: Details)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    @JvmSuppressWildcards
    suspend fun insertUserDetailIgnore(user: Details)

    @Query("SELECT * FROM details WHERE login_details=:login LIMIT 1")
    fun getUsersDetail(login:String): Details

    @Query("DELETE FROM details")
    suspend fun deleteUserDetails()

    @Update
    suspend fun updateUserDetail(details: Details)
}