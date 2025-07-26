package com.example.data.repository

import android.util.Log
import com.example.data.storage.local.dao.TaskDao
import com.example.data.storage.local.entity.TaskEntity
import com.example.data.storage.model.TaskDto
import com.example.data.storage.model.UnsplashPhotoDto
import com.example.data.storage.remote.api.TaskApi
import com.example.data.storage.remote.api.UnsplashApi
import com.example.domain.model.Task
import com.example.domain.model.UnsplashPhoto
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

    // --- Мапперы (без изменений) ---
    private fun TaskDto.toEntity(imageUrl: String?): TaskEntity {
        return TaskEntity(id = this.id, title = this.title, completed = this.completed, imageUrl = imageUrl, isLocalOnly = false, createdAt = System.currentTimeMillis())
    }

    private fun TaskEntity.toDomain(): Task {
        return Task(id = this.id, title = this.title, status = this.completed, imageUrl = this.imageUrl, isLocalOnly = this.isLocalOnly)
    }

    private fun Task.toEntity(isLocalOnly: Boolean = false, isModified: Boolean = false): TaskEntity {
        return TaskEntity(id = this.id, title = this.title, completed = this.status, imageUrl = this.imageUrl, isLocalOnly = isLocalOnly, isModified = isModified, createdAt = System.currentTimeMillis())
    }

    private fun UnsplashPhotoDto.toDomain(): UnsplashPhoto {
        return UnsplashPhoto(
            id = this.id,
            regularUrl = this.urls.regular,
            smallUrl = this.urls.small
        )
    }

    // --- Вспомогательная функция для загрузки случайных фото Unsplash ---
    // Устанавливаем default count = 100, чтобы соответствовать количеству задач
// data/repository/TaskRepositoryImpl.kt

    private suspend fun fetchRandomPhotosInternal(count: Int = 100): List<UnsplashPhotoDto> = coroutineScope {
        val batchSize = 30 // Максимум фото за один запрос к Unsplash API
        val numberOfBatches = (count + batchSize - 1) / batchSize // Сколько запросов нужно сделать

        Log.d("UnsplashFetch", "Attempting to fetch $count photos in $numberOfBatches batches.")

        val batches = (1..numberOfBatches).map { batchIndex ->
            async {
                try {
                    Log.d("UnsplashFetch", "Fetching batch $batchIndex...")
                    val photosInBatch = unsplashApi.getRandomPhotos(batchSize, unsplashApiKey)
                    Log.d("UnsplashFetch", "Batch $batchIndex fetched ${photosInBatch.size} photos.")
                    photosInBatch
                } catch (e: Exception) {
                    Log.e("UnsplashFetch", "❌ Error fetching photos (batch $batchIndex): ${e.message}")
                    emptyList()
                }
            }
        }
        // Собираем все фото, перемешиваем и берем только нужное количество
        val allFetchedPhotos = batches.awaitAll().flatten()
        val finalPhotos = allFetchedPhotos.shuffled().take(count)
        Log.d("UnsplashFetch", "Total photos fetched (before shuffle/take): ${allFetchedPhotos.size}")
        Log.d("UnsplashFetch", "Final photos to return: ${finalPhotos.size}")
        return@coroutineScope finalPhotos
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
                // 1. Загружаем задачи из сети, ограничивая их до 100
                val networkTasks = taskApi.getTasks().take(100) // <-- Ограничиваем до 100 задач!

                // 2. Загружаем столько же случайных фото из Unsplash, сколько у нас задач
                val photos = fetchRandomPhotosInternal(count = networkTasks.size) // <-- Точное количество фото

                // 3. Очищаем старые сетевые задачи в Room
                taskDao.deleteAllNonLocalTasks()

                // 4. Преобразуем и сохраняем сетевые задачи в Room, присваивая случайные картинки
                val tasksToInsert = networkTasks.mapIndexed { index, dto ->
                    val imageUrlForNetworkTask = if (photos.isNotEmpty()) {
                        // Используем фото по индексу. Если фото меньше, чем задач, они будут повторяться циклически.
                        photos[index % photos.size].urls.small
                    } else {
                        // Если фото вообще не загрузились, используем общую заглушку
                        "https://via.placeholder.com/50"
                    }
                    dto.toEntity(imageUrl = imageUrlForNetworkTask)
                }

                taskDao.insertAllTasks(tasksToInsert)
                Log.d("TaskRepository", "✅ Tasks successfully refreshed from network and saved to Room. Count: ${tasksToInsert.size}")
                true
            } catch (e: Exception) {
                Log.e("TaskRepository", "❌ Error refreshing tasks from network: ${e.message}")
                false
            }
        }
    }

    override suspend fun addTask(task: Task): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val taskEntity = task.toEntity(isLocalOnly = true)
                taskDao.insertTask(taskEntity)
                Log.d("TaskRepository", "✅ Задача '${task.title}' успешно добавлена в Room (локально).")
                true
            } catch (e: Exception) {
                Log.e("TaskRepository", "❌ Ошибка при добавлении задачи '${task.title}' в Room: ${e.message}")
                false
            }
        }
    }

    override suspend fun updateTask(task: Task): Boolean {
        Log.w("TaskRepository", "updateTask not yet implemented. This will update an existing task in Room and set isModified = true.")
        return false
    }

    override suspend fun deleteTask(id: Int): Boolean {
        Log.w("TaskRepository", "deleteTask not yet implemented. This will delete a task from Room.")
        return false
    }

    override suspend fun getUnsplashPhotos(count: Int): List<UnsplashPhoto> {
        return withContext(Dispatchers.IO) {
            try {
                // Этот метод используется для UI-выбора фото, поэтому здесь count передается из ViewModel (обычно 30)
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