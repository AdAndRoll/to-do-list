package com.example.data.repository

import com.example.data.storage.remote.api.TaskApi
import com.example.data.storage.remote.api.UnsplashApi
import com.example.domain.model.Task
import com.example.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Named

class TaskRepositoryImpl @Inject constructor(
    private val taskApi :TaskApi,
    private val unsplashApi: UnsplashApi,
    @Named("UnsplashApiKey") private val unsplashApiKey: String
):TaskRepository {
    override fun getTasks(): Flow<List<Task>> {
        return flow {
            val tasks = taskApi.getTasks().map { dto ->
                val photo = unsplashApi.getRandomPhoto(unsplashApiKey)
                Task(
                    id = dto.id,
                    title = dto.title,
                    status = dto.completed,
                    imageUrl = photo.urls.small
                )
            }
            emit(tasks)
        }
    }

    override suspend fun addTask(task: Task): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTask(id: Int): Boolean {
        TODO("Not yet implemented")
    }


}