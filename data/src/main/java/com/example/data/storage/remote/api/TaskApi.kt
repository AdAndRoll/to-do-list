package com.example.data.storage.remote.api

import com.example.data.storage.model.TaskDto
import retrofit2.http.GET

interface TaskApi {
    @GET("todos")
    suspend fun getTasks(): List<TaskDto>
}