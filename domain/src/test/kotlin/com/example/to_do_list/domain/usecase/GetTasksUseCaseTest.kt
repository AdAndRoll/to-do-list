package com.example.domain.usecases

import com.example.domain.model.Task
import com.example.domain.repository.TaskRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetTasksUseCaseTest {
    private lateinit var useCase: GetTasksUseCase
    private lateinit var repository: TaskRepository

    @Before
    fun setUp() {
        repository = mock()
        useCase = GetTasksUseCase(repository)
    }

    @Test
    fun `execute returns tasks from repository`() = runBlocking {
        val tasks = listOf(
            Task(id = 1, title = "Task 1", status = false, imageUrl = null),
            Task(id = 2, title = "Task 2", status = true, imageUrl = "https://example.com/image.jpg")
        )
        whenever(repository.getTasks()).thenReturn(flowOf(tasks))
        val result = useCase.execute().toList()
        assertEquals(listOf(tasks), result)
    }
}