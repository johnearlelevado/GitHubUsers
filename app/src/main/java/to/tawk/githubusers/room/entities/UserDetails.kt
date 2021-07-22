package to.tawk.githubusers.room.entities

import androidx.room.Embedded

/**
 * Combination of user corresponding details specifically made to facilitate
 * the INNER JOIN on UserDao.getUserDetailsSearchPaginated()
 * */
data class UserDetails(
    @Embedded val user: User,
    @Embedded val details: Details
)