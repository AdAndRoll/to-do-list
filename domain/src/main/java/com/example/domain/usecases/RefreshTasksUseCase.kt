package com.example.domain.usecases

import com.example.domain.repository.TaskRepository

class RefreshTasksUseCase(private val taskRepository: TaskRepository) {
    suspend fun execute(): Boolean {
        return taskRepository.refreshTasksFromNetworkAndSaveLocally()
    }
}