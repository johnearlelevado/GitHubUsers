package to.tawk.githubusers.room

import androidx.room.Database
import androidx.room.RoomDatabase
import to.tawk.githubusers.room.dao.UserDao
import to.tawk.githubusers.room.dao.DetailsDao
import to.tawk.githubusers.room.entities.User
import to.tawk.githubusers.room.entities.Details

@Database(entities = [User::class,Details::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userDetailsDao(): DetailsDao
}