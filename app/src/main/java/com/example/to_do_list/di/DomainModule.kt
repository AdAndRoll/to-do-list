package com.example.to_do_list.di

import com.example.domain.repository.TaskRepository
import com.example.domain.usecases.GetTasksUseCase
import com.example.domain.usecases.GetUnsplashPhotosUseCase // <-- НОВЫЙ ИМПОРТ
import com.example.domain.usecases.AddTaskUseCase // <-- НОВЫЙ ИМПОРТ (раскомментируйте, когда будете готовы)
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    @Singleton
    fun provideGetTasksUseCase(taskRepository: TaskRepository): GetTasksUseCase {
        return GetTasksUseCase(taskRepository)
    }

    @Provides
    @Singleton
    fun provideGetUnsplashPhotosUseCase(taskRepository: TaskRepository): GetUnsplashPhotosUseCase {
        return GetUnsplashPhotosUseCase(taskRepository)
    }


     @Provides
     @Singleton
     fun provideAddTaskUseCase(taskRepository: TaskRepository): AddTaskUseCase {
         return AddTaskUseCase(taskRepository)
     }
}