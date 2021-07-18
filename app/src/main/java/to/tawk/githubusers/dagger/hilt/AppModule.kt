package to.tawk.githubusers.dagger.hilt

import android.content.Context
import androidx.room.Room
import to.tawk.githubusers.BuildConfig
import to.tawk.githubusers.api.ApiCore
import to.tawk.githubusers.api.common.scheduler.AppSchedulerProvider
import to.tawk.githubusers.api.common.scheduler.SchedulerProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import to.tawk.githubusers.api.users.service.UsersService
import to.tawk.githubusers.room.AppDatabase
import to.tawk.githubusers.repository.RepositoryImpl
import to.tawk.githubusers.repository.Repository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideRetrofit() = ApiCore.getRetrofit(BuildConfig.API_URL)

    @Singleton
    @Provides
    fun provideScheduler():SchedulerProvider = AppSchedulerProvider()

    @Singleton
    @Provides
    fun provideUsersService(retrofit : Retrofit):UsersService
            = retrofit.create(UsersService::class.java)

    @Singleton
    @Provides
    fun provideRepository(db:AppDatabase,usersService: UsersService): Repository
            = RepositoryImpl(db,usersService)

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext app:Context): AppDatabase = Room
        .databaseBuilder(app,
            AppDatabase::class.java,
            "github_user_viewer.db")
        .allowMainThreadQueries()
        .build()
}