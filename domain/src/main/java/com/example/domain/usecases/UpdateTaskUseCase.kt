package com.example.domain.usecases
import com.example.domain.model.Task
import com.example.domain.repository.TaskRepository

// В доменном слое не должно быть никаких @Inject
// Зависимости должны быть переданы через конструктор
class UpdateTaskUseCase(
    private val taskRepository: TaskRepository
) {

    suspend fun execute(task: Task): Boolean {
        return taskRepository.updateTask(task)
    }
}