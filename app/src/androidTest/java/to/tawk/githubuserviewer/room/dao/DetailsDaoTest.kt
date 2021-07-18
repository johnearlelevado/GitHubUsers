package to.tawk.githubuserviewer.room.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import to.tawk.githubuserviewer.mock.UserDetailsMock
import to.tawk.githubuserviewer.room.AppDatabase
import to.tawk.githubuserviewer.room.entities.Details
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DetailsDaoTest {
    private lateinit var detailsDao: DetailsDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, AppDatabase::class.java).build()
        detailsDao = db.userDetailsDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testInsertDetailsIfSuccessful()  {
        val details = UserDetailsMock.getDetails()
        runBlocking {
            detailsDao.insertUserDetail(details)
        }
        val detail = detailsDao.getUsersDetail(login = "fake-login")
        assertThat(detail.login == "fake-login").isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateDetailsIfSuccessful()  {
        val details = UserDetailsMock.getDetails()
        runBlocking {
            detailsDao.insertUserDetail(details)
        }
        val detail = detailsDao.getUsersDetail(login = "fake-login")
        assertThat(detail.note == "").isTrue()

        val testText = "new text here"
        detail.note = testText
        runBlocking {
            detailsDao.updateUserDetail(detail)
        }

        val detailWithNote = detailsDao.getUsersDetail(login = "fake-login")
        assertThat(detailWithNote.note == testText).isTrue()
    }
}