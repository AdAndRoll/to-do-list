package com.example.domain.repository

import com.example.domain.model.Task
import com.example.domain.model.UnsplashPhoto
import kotlinx.coroutines.flow.Flow


interface TaskRepository {
    // Этот метод теперь исключительно для получения данных из локального хранилища (Room).
    // Он не инициирует сетевые запросы.
    fun getTasks(): Flow<List<Task>>

    // Новый метод: отвечает за загрузку актуальных данных из сети
    // и их сохранение в локальном хранилище (Room).
    // Он не возвращает Flow, это "одноразовая" операция синхронизации.
    suspend fun refreshTasksFromNetworkAndSaveLocally(): Boolean

    // Методы для будущей реализации офлайн-изменений:
    // Добавление новой задачи (только локально)
    suspend fun addTask(task: Task): Boolean

    // Обновление существующей задачи (только локально, с флагом isModified)
    suspend fun updateTask(task: Task): Boolean // Добавляем этот метод

    // Удаление задачи (только локально)
    suspend fun deleteTask(id: Int): Boolean

    suspend fun getUnsplashPhotos(count: Int): List<UnsplashPhoto>
}