package com.example.data.repository

import android.util.Log
import com.example.data.storage.local.dao.TaskDao
import com.example.data.storage.local.entity.TaskEntity // Убедитесь, что путь правильный
import com.example.data.storage.model.TaskDto
import com.example.data.storage.model.UnsplashPhotoDto
import com.example.data.storage.remote.api.TaskApi
import com.example.data.storage.remote.api.UnsplashApi
import com.example.domain.model.Task
import com.example.domain.model.UnsplashPhoto // **ВАЖНО: Добавьте этот импорт**
import com.example.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import kotlin.random.Random

class TaskRepositoryImpl @Inject constructor(
    private val taskApi: TaskApi,
    private val unsplashApi: UnsplashApi,
    private val taskDao: TaskDao,
    @Named("UnsplashApiKey") private val unsplashApiKey: String
) : TaskRepository {

    // --- Мапперы (преобразование между слоями) ---

    // Преобразование TaskDto (из сети) в TaskEntity (для Room)
    private fun TaskDto.toEntity(imageUrl: String?): TaskEntity {
        // Задачи из сети всегда isLocalOnly = false
        return TaskEntity(id = this.id, title = this.title, completed = this.completed, imageUrl = imageUrl, isLocalOnly = false)
    }

    // Преобразование TaskEntity (из Room) в Task (для доменного слоя)
    private fun TaskEntity.toDomain(): Task {
        // Используем поле isLocalOnly из TaskEntity
        return Task(id = this.id, title = this.title, status = this.completed, imageUrl = this.imageUrl, isLocalOnly = this.isLocalOnly)
    }

    // Преобразование Task (из доменного слоя, для локальных изменений) в TaskEntity (для Room)
    private fun Task.toEntity(isLocalOnly: Boolean = false, isModified: Boolean = false): TaskEntity {
        // Передаем значение isLocalOnly из доменной модели
        return TaskEntity(id = this.id, title = this.title, completed = this.status, imageUrl = this.imageUrl, isLocalOnly = isLocalOnly, isModified = isModified)
    }

    // **НОВЫЙ МАППЕР: UnsplashPhotoDto -> UnsplashPhoto (для доменного слоя)**
    private fun UnsplashPhotoDto.toDomain(): UnsplashPhoto {
        return UnsplashPhoto(
            id = this.id, // Теперь получаем ID
            regularUrl = this.urls.regular, // Теперь получаем regular URL
            smallUrl = this.urls.small
        )
    }

    // --- Вспомогательная функция для загрузки случайных фото Unsplash ---
    // **ВАЖНО: Переименовано из `fetchRandomPhotos` в `fetchRandomPhotosInternal`**
    private suspend fun fetchRandomPhotosInternal(count: Int = 100): List<UnsplashPhotoDto> = coroutineScope {
        val batchSize = 30
        val batches = (1..((count + batchSize - 1) / batchSize)).map {
            async {
                try {
                    unsplashApi.getRandomPhotos(batchSize, unsplashApiKey)
                } catch (e: Exception) {
                    Log.w("TaskRepository", "⚠️ Error fetching photos (batch $it): ${e.message}")
                    emptyList()
                }
            }
        }
        return@coroutineScope batches.awaitAll().flatten().shuffled().take(count)
    }

    // --- Основные методы репозитория (реализация TaskRepository) ---

    override fun getTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun refreshTasksFromNetworkAndSaveLocally(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val dtos = taskApi.getTasks()
                // **Используем новое имя: fetchRandomPhotosInternal**
                val photos = fetchRandomPhotosInternal(count = dtos.size.coerceAtMost(100))

                val tasksToInsert = dtos.mapIndexed { index, dto ->
                    // **ИСПРАВЛЕНИЕ ЗДЕСЬ:** Добавляем 'id = "placeholder_id_$index"'
                    val photo = photos.getOrNull(index) ?: UnsplashPhotoDto(
                        id = "placeholder_id_${index}", // <--- ДОБАВЛЕНО!
                        urls = UnsplashPhotoDto.Urls(small = "https://via.placeholder.com/50", regular = "https://via.placeholder.com/100")
                    )
                    dto.toEntity(photo.urls.small) // Для imageUrl задачи пока используем small
                }

                taskDao.deleteAllNonLocalTasks()
                taskDao.insertAllTasks(tasksToInsert)
                Log.d("TaskRepository", "✅ Tasks successfully refreshed from network and saved to Room.")
                true
            } catch (e: Exception) {
                Log.e("TaskRepository", "❌ Error refreshing tasks from network: ${e.message}")
                false
            }
        }
    }

    // --- Заглушки для будущих методов (пока не меняем) ---
    override suspend fun addTask(task: Task): Boolean {
        Log.w("TaskRepository", "addTask not yet implemented. This will add a local-only task to Room.")
        // TODO: Implement adding a new task to Room and setting isLocalOnly = true
        return false
    }

    override suspend fun updateTask(task: Task): Boolean {
        Log.w("TaskRepository", "updateTask not yet implemented. This will update an existing task in Room and set isModified = true.")
        // TODO: Implement updating an existing task in Room and setting isModified = true
        return false
    }

    override suspend fun deleteTask(id: Int): Boolean {
        Log.w("TaskRepository", "deleteTask not yet implemented. This will delete a task from Room.")
        // TODO: Implement deleting a task from Room
        return false
    }

    override suspend fun getUnsplashPhotos(count: Int): List<UnsplashPhoto> {
        return withContext(Dispatchers.IO) {
            try {
                fetchRandomPhotosInternal(count).map { it.toDomain() }
            } catch (e: Exception) {
                Log.e("TaskRepository", "❌ Error getting Unsplash photos for UI: ${e.message}")
                emptyList()
            }
        }
    }

    private suspend fun generateUniqueLocalId(): Int = withContext(Dispatchers.IO) {
        Random.nextInt(Int.MIN_VALUE, -1)
    }
}