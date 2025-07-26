package com.example.to_do_list.ui.tasklist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Task
import com.example.domain.model.UnsplashPhoto // <-- НОВЫЙ ИМПОРТ
import com.example.domain.usecases.AddTaskUseCase
import com.example.domain.usecases.GetTasksUseCase
import com.example.domain.usecases.GetUnsplashPhotosUseCase // <-- НОВЫЙ ИМПОРТ
import com.example.to_do_list.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val getUnsplashPhotosUseCase: GetUnsplashPhotosUseCase,
    private val addTaskUseCase: AddTaskUseCase// <-- ИНЖЕКТИРУЕМ GetUnsplashPhotosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState(isLoading = true, tasks = emptyList(), error = null))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // НОВОЕ СОСТОЯНИЕ ДЛЯ СПИСКА ФОТО UNSPLASH
    private val _unsplashPhotos = MutableStateFlow<List<UnsplashPhoto>>(emptyList())
    val unsplashPhotos: StateFlow<List<UnsplashPhoto>> = _unsplashPhotos.asStateFlow()

    // НОВОЕ СОСТОЯНИЕ ДЛЯ ИНДИКАТОРА ЗАГРУЗКИ ФОТО
    private val _isPhotosLoading = MutableStateFlow(false)
    val isPhotosLoading: StateFlow<Boolean> = _isPhotosLoading.asStateFlow()

    init {
        observeTasks()
        refreshTasksFromNetwork()
    }

    // --- Методы для UI-взаимодействия ---



    fun retry() {
        refreshTasksFromNetwork()
        _uiState.value = _uiState.value.copy(error = null, isLoading = true)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }


    // НОВЫЙ МЕТОД: Загрузка фото из Unsplash
    fun loadUnsplashPhotos() {
        viewModelScope.launch {
            _isPhotosLoading.value = true
            try {
                // Загружаем 30 фото (можно настроить количество)
                val photos = getUnsplashPhotosUseCase.execute(count = 30)
                _unsplashPhotos.value = photos
                Log.d("TaskViewModel", "📸 Загружено ${photos.size} фото из Unsplash.")
            } catch (e: Exception) {
                Log.e("TaskViewModel", "❌ Ошибка загрузки фото Unsplash: ${e.message}")
                // Можно установить отдельное состояние ошибки для фото, если нужно
                _uiState.value = _uiState.value.copy(
                    error = "Не удалось загрузить изображения Unsplash."
                )
            } finally {
                _isPhotosLoading.value = false
            }
        }
    }

    // НОВЫЙ МЕТОД: Очистка списка фото (при закрытии диалога)
    fun clearUnsplashPhotos() {
        _unsplashPhotos.value = emptyList()
    }

    // --- Приватные методы логики ViewModel (без изменений) ---

    private fun observeTasks() {
        getTasksUseCase.execute()
            .onEach { tasks ->
                _uiState.value = _uiState.value.copy(
                    tasks = tasks,
                    isLoading = false,
                    error = null
                )
                Log.d("TaskViewModel", "🧩 UI обновлён из Room: загружено ${tasks.size} задач")
            }
            .catch { e ->
                _uiState.value = UiState(
                    error = e.message ?: "Ошибка загрузки данных из кэша",
                    isLoading = false,
                    tasks = emptyList()
                )
                Log.e("TaskViewModel", "❌ Ошибка при наблюдении за задачами из Room: ${e.message}")
            }
            .launchIn(viewModelScope)
    }

    fun refreshTasksFromNetwork() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            Log.d("TaskViewModel", "🔄 Запускаем обновление задач из сети...")
            val success = getTasksUseCase.refreshTasksFromNetwork()
            if (success) {
                Log.d("TaskViewModel", "✅ Задачи успешно обновлены из сети и сохранены в Room.")
            } else {
                Log.e("TaskViewModel", "❌ Ошибка при обновлении задач из сети.")
                _uiState.value = _uiState.value.copy(
                    error = "Не удалось обновить данные с сервера.",
                )
            }
            if (_uiState.value.tasks.isEmpty() && !success) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun addNewTask(title: String, imageUrl: String?) {
        viewModelScope.launch {
            if (title.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    error = "Заголовок задачи не может быть пустым."
                )
                return@launch
            }

            val newTask = Task(
                id = 0, // ID 0 указывает Room на автогенерацию
                title = title,
                status = false,
                imageUrl = imageUrl,
                isLocalOnly = true // Помечаем как локальную
            )

            try {
                val success = addTaskUseCase.execute(newTask) // <-- Вызов Use Case
                if (success) {
                    Log.d("TaskViewModel", "✅ Задача '$title' успешно добавлена локально.")
                    // UI обновится автоматически, так как observeTasks слушает изменения в Room
                } else {
                    Log.e("TaskViewModel", "❌ Не удалось добавить задачу '$title'.")
                    _uiState.value = _uiState.value.copy(
                        error = "Не удалось добавить задачу."
                    )
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "❌ Ошибка при вызове AddTaskUseCase: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = "Произошла ошибка при добавлении задачи: ${e.message}"
                )
            }
        }
    }
}