package com.example.data.TaskRepositoryImpl

import com.example.data.repository.TaskRepositoryImpl
import com.example.data.storage.model.TaskDto
import com.example.data.storage.model.UnsplashPhotoDto
import com.example.data.storage.model.UnsplashUrlsDto
import com.example.data.storage.remote.api.TaskApi
import com.example.data.storage.remote.api.UnsplashApi
import com.example.domain.model.Task
import com.example.domain.repository.TaskRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class TaskRepositoryImplTest {
    private lateinit var repository: TaskRepository
    private lateinit var taskApi: TaskApi
    private lateinit var unsplashApi: UnsplashApi
    private val unsplashApiKey = "test_api_key"

    @Before
    fun setUp() {
        taskApi = mock()
        unsplashApi = mock()
        repository = TaskRepositoryImpl(taskApi, unsplashApi, unsplashApiKey)
    }

    @Test
    fun `getTasks returns mapped tasks from api with unsplash image`() = runBlocking {
        // Arrange
        val dtos = listOf(
            TaskDto(id = 1, title = "Task 1", completed = false),
            TaskDto(id = 2, title = "Task 2", completed = true)
        )
        val photo = UnsplashPhotoDto(urls = UnsplashUrlsDto(small = "https://example.com/image.jpg"))
        val expectedTasks = listOf(
            Task(id = 1, title = "Task 1", status = false, imageUrl = "https://example.com/image.jpg"),
            Task(id = 2, title = "Task 2", status = true, imageUrl = "https://example.com/image.jpg")
        )
        whenever(taskApi.getTasks()).thenReturn(dtos)
        whenever(unsplashApi.getRandomPhoto(unsplashApiKey)).thenReturn(photo)

        // Act
        val result = repository.getTasks().toList()

        // Assert
        assertEquals(listOf(expectedTasks), result)
    }
}