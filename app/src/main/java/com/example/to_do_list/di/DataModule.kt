package com.example.to_do_list.di

import com.example.data.repository.TaskRepositoryImpl
import com.example.data.storage.remote.api.TaskApi
import com.example.data.storage.remote.api.UnsplashApi
import com.example.domain.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
        return "HgTZyDESWo_5dUDHLZqYjJ048itPufuXVcwb3a5qNRw" // Замените на ваш API-ключ
    }

    @Provides
    @Singleton
    fun provideTaskRepository(impl: TaskRepositoryImpl): TaskRepository {
        return impl
    }

}