package to.tawk.githubuserviewer.room.entities

import androidx.room.Embedded

data class UserDetails(
    @Embedded val user: User,
    @Embedded val details: Details
)