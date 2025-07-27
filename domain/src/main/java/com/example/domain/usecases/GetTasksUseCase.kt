package com.example.domain.usecases

import com.example.domain.model.Task
import com.example.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class GetTasksUseCase(private val taskRepository: TaskRepository) { // Принимает зависимость через конструктор

    fun execute(): Flow<List<Task>> {
        return taskRepository.getTasks()
    }
}