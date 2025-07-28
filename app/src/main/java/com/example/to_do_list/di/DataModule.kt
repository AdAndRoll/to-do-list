package com.example.to_do_list.di

import android.content.Context
import androidx.room.Room
import com.example.data.repository.TaskRepositoryImpl
import com.example.data.storage.local.dao.TaskDao
import com.example.data.storage.local.database.AppDatabase
import com.example.data.storage.remote.api.TaskApi
import com.example.data.storage.remote.api.UnsplashApi
import com.example.domain.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.internal.wait
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    @Named("JsonPlaceholderRetrofit")
    fun provideJsonPlaceholderRetrofit():Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("UnsplashRetrofit")
    fun provideUnsplashRetrofit():Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://api.unsplash.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTaskApi(@Named("JsonPlaceholderRetrofit") retrofit: Retrofit):TaskApi{
        return retrofit.create(TaskApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUnsplashApi(@Named("UnsplashRetrofit") retrofit: Retrofit):UnsplashApi{
        return retrofit.create(UnsplashApi::class.java)
    }

    @Provides
    @Singleton
    @Named("UnsplashApiKey")
    fun provideUnsplashApiKey(): String {
        return "92SsgcxvYhfODoIykOmLj6BopX_e9M0fzhrEoFXP1FU" // Замените на ваш API-ключ
    }


    //Room
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "tasks_database" // Имя вашей базы данных
        )
            // Если вы меняете схему (version), но не хотите терять данные, используйте миграции.
            //
            //
            .fallbackToDestructiveMigration(true) // Используйте ТОЛЬКО для разработки! Удаляет базу данных при изменении версии.
            .build()
    }

    @Provides
    @Singleton
    fun provideTaskDao(appDatabase: AppDatabase): TaskDao {
        return appDatabase.taskDao()
    }

    // Обновите provideTaskRepository, чтобы включить TaskDao
    @Provides
    @Singleton
    fun provideTaskRepository(
        taskApi: TaskApi,
        unsplashApi: UnsplashApi,
        taskDao: TaskDao, // <-- Теперь TaskDao инжектируется
        @Named("UnsplashApiKey") unsplashApiKey: String
    ): TaskRepository {
        return TaskRepositoryImpl(taskApi, unsplashApi, taskDao, unsplashApiKey)
    }


}
