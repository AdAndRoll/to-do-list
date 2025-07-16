package com.example.to_do_list.domain.model

import com.example.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskTest {
    @Test
    fun `Task creation and equality`() {
        val task1 = Task(id = 1, title = "Test Task", status = false,imageUrl = null)
        val task2 = Task(id = 1, title = "Test Task", status = false,imageUrl = null)
        val task3 = Task(id = 2, title = "Different Task", status = true,imageUrl = "https://example.com/image.jpg")

        assertEquals(task1, task2) // Проверяем равенство одинаковых задач
        assertEquals(false, task1 == task3) // Проверяем неравенство разных задач
        assertEquals(1, task1.id)
        assertEquals("Test Task", task1.title)
        assertEquals(false, task1.status)
        assertEquals(null, task1.imageUrl)
        assertEquals("https://example.com/image.jpg", task3.imageUrl)
    }
}