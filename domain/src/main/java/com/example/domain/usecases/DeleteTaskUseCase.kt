package com.example.domain.usecases

import com.example.domain.model.Task
import com.example.domain.repository.TaskRepository

class DeleteTaskUseCase(private val taskRepository: TaskRepository) {
        suspend fun execute(id: Int): Boolean {
            return taskRepository.deleteTask(id)
        }
    }
