package to.tawk.githubusers.room.entities

import androidx.room.Embedded

data class UserDetails(
    @Embedded val user: User,
    @Embedded val details: Details
)