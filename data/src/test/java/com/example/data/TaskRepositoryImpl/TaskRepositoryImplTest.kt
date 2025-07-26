package com.example.data.repository

import app.cash.turbine.test
import com.example.data.storage.model.TaskDto
import com.example.data.storage.model.UnsplashPhotoDto
import com.example.data.storage.remote.api.TaskApi
import com.example.data.storage.remote.api.UnsplashApi
import com.example.domain.model.Task
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class TaskRepositoryImplTest {

    private lateinit var taskRepository: TaskRepositoryImpl
    private val taskApi: TaskApi = mockk()
    private val unsplashApi: UnsplashApi = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val fakeTasks = listOf(
        TaskDto(1, "Task 1", false),
        TaskDto(2, "Task 2", true),
        TaskDto(3, "Task 3", false)
    )

    private val fakePhotos = listOf(
        UnsplashPhotoDto(UnsplashPhotoDto.Urls("url1")),
        UnsplashPhotoDto(UnsplashPhotoDto.Urls("url2")),
        UnsplashPhotoDto(UnsplashPhotoDto.Urls("url3"))
    )

    @Before
    fun setup() {
        taskRepository = TaskRepositoryImpl(taskApi, unsplashApi, "fake_key")
    }

    @Test
    fun `getTasks returns tasks with correct mapping`() = runTest(testDispatcher) {
        coEvery { taskApi.getTasks() } returns fakeTasks
        coEvery { unsplashApi.getRandomPhotos(any(), any()) } returns fakePhotos

        val results = taskRepository.getTasks().toList().flatten()

        assertEquals(3, results.size)
        assertEquals("Task 1", results[0].title)
        assertTrue(results[0].imageUrl?.contains("url") == true)
    }

    @Test
    fun `getTasks returns placeholder image when not enough photos`() = runTest(testDispatcher) {
        coEvery { taskApi.getTasks() } returns fakeTasks
        coEvery { unsplashApi.getRandomPhotos(any(), any()) } returns listOf() // simulate empty photo list

        val results = taskRepository.getTasks().toList().flatten()

        assertEquals(3, results.size)
        assertEquals("https://via.placeholder.com/50", results[0].imageUrl)
    }

    @Test
    fun `getTasks handles empty task list`() = runTest(testDispatcher) {
        coEvery { taskApi.getTasks() } returns emptyList()
        coEvery { unsplashApi.getRandomPhotos(any(), any()) } returns fakePhotos

        val results = taskRepository.getTasks().toList().flatten()
        assertTrue(results.isEmpty())
    }

    @Test
    fun `getTasks handles photo API failure gracefully`() = runTest(testDispatcher) {
        coEvery { taskApi.getTasks() } returns fakeTasks
        coEvery { unsplashApi.getRandomPhotos(any(), any()) } throws RuntimeException("Photo error")

        val results = taskRepository.getTasks().toList().flatten()
        assertEquals(3, results.size)
        assertEquals("https://via.placeholder.com/50", results[0].imageUrl)
    }

    @Test
    fun `getTasks handles task API failure gracefully`() = runTest(testDispatcher) {
        coEvery { taskApi.getTasks() } throws RuntimeException("Task API error")
        coEvery { unsplashApi.getRandomPhotos(any(), any()) } returns fakePhotos

        val results = taskRepository.getTasks().toList().flatten()
        assertTrue(results.isEmpty())
    }

    @Test
    fun `getTasks emits in chunks`() = runTest(testDispatcher) {
        val manyTasks = (1..25).map { TaskDto(it, "Task $it", it % 2 == 0) }
        val manyPhotos = (1..25).map { UnsplashPhotoDto(UnsplashPhotoDto.Urls("url$it")) }

        coEvery { taskApi.getTasks() } returns manyTasks
        coEvery { unsplashApi.getRandomPhotos(any(), any()) } returns manyPhotos

        val chunks = mutableListOf<List<Task>>()

        taskRepository.getTasks().test {
            while (true) {
                val item = awaitItem()
                chunks.add(item)
                if (item.size == 25) break
            }
            awaitComplete()
        }

        assertTrue(chunks.size >= 2)
        assertEquals(25, chunks.last().size)
    }
}
