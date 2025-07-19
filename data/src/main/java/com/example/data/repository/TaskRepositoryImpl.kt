package com.example.data.repository

import android.util.Log
import com.example.data.storage.model.TaskDto
import com.example.data.storage.model.UnsplashPhotoDto

import com.example.data.storage.remote.api.TaskApi
import com.example.data.storage.remote.api.UnsplashApi
import com.example.domain.model.Task
import com.example.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class TaskRepositoryImpl @Inject constructor(
    private val taskApi: TaskApi,
    private val unsplashApi: UnsplashApi,
    @Named("UnsplashApiKey") private val unsplashApiKey: String
) : TaskRepository {

    private suspend fun fetchRandomPhotos(count: Int = 100): List<UnsplashPhotoDto> = coroutineScope {
        val batchSize = 30
        val batches = (1..((count + batchSize - 1) / batchSize)).map {
            async {
                try {
                    unsplashApi.getRandomPhotos(batchSize, unsplashApiKey)
                } catch (e: Exception) {
                    Log.w("TaskRepository", "⚠️ Ошибка при загрузке фото (batch $it): ${e.message}")
                    emptyList()
                }
            }
        }
        return@coroutineScope batches.awaitAll().flatten().shuffled().take(count)
    }



    override fun getTasks(): Flow<List<Task>> = channelFlow {
        val resultList = mutableListOf<Task>()
        val mutex = Mutex()

        Log.d("TaskRepository", "🔄 Начинаем загрузку задач с API")

        val dtos = withContext(Dispatchers.IO) {
            try {
                taskApi.getTasks()
            } catch (e: Exception) {
                Log.e("TaskRepository", "❌ Ошибка загрузки задач: ${e.message}")
                emptyList()
            }
        }

        Log.d("TaskRepository", "✅ Получено ${dtos.size} задач. Загружаем изображения...")

        // Загружаем 100 случайных фото заранее (или меньше, если задач меньше)
        val photos = try {
            fetchRandomPhotos(count = dtos.size.coerceAtMost(100))
        } catch (e: Exception) {
            Log.w("TaskRepository", "❗ Ошибка при загрузке фото: ${e.message}")
            emptyList()
        }

        dtos.mapIndexed { index, dto ->
            launch(Dispatchers.IO) {
                val photo = photos.getOrNull(index) ?: UnsplashPhotoDto(urls = UnsplashPhotoDto.Urls(small = "https://via.placeholder.com/50"))
                Log.d("TaskRepository", "[$index] Фото для: ${dto.title} - ${photo.urls.small}")

                val task = Task(
                    id = dto.id,
                    title = dto.title,
                    status = dto.completed,
                    imageUrl = photo.urls.small
                )

                mutex.withLock {
                    resultList.add(task)
                    Log.d("TaskRepository", "[$index] Задача добавлена: ${task.title}")

                    if (resultList.size % 10 == 0 || resultList.size == dtos.size) {
                        send(resultList.toList())
                        Log.d("TaskRepository", "📦 Порция отправлена: ${resultList.size} задач")
                    }
                }
            }
        }.joinAll()

        Log.d("TaskRepository", "✅ Все задачи загружены.")
    }.flowOn(Dispatchers.Default)







    override suspend fun addTask(task: Task): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTask(id: Int): Boolean {
        TODO("Not yet implemented")
    }
}

