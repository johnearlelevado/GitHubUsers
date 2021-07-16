package to.tawk.githubuserviewer.room

import androidx.room.Database
import androidx.room.RoomDatabase
import to.tawk.githubuserviewer.room.dao.UserDao
import to.tawk.githubuserviewer.room.dao.DetailsDao
import to.tawk.githubuserviewer.room.entities.User
import to.tawk.githubuserviewer.room.entities.Details

@Database(entities = [User::class,Details::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userDetailsDao(): DetailsDao
}