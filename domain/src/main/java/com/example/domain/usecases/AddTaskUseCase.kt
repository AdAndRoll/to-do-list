package com.example.domain.usecases

import com.example.domain.model.Task
import com.example.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class AddTaskUseCase (private val taskRepository: TaskRepository) {
    suspend fun execute(task: Task): Boolean {
        return taskRepository.addTask(task)
    }
}