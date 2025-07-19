package com.example.to_do_list.ui


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Task
import com.example.domain.usecases.GetTasksUseCase
import com.example.to_do_list.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class TaskViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState(isLoading = true))
    val uiState: StateFlow<UiState> = _uiState

    init {
        loadTasks()
    }

    fun retry() {
        loadTasks()
    }


    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = UiState(isLoading = true, tasks = emptyList(), error = null)

            try {
                val allTasks = mutableListOf<Task>()

                getTasksUseCase.execute().collect { newTasksPortion ->
                    allTasks.addAll(newTasksPortion)
                    _uiState.value = _uiState.value.copy(
                        tasks = allTasks.toList(),
                        isLoading = true, // продолжаем показывать загрузку пока идут порции
                        error = null
                    )
                    Log.d("TaskViewModel", "🧩 UI обновлён: загружено ${allTasks.size} задач")
                }

                // Когда поток завершился (collect вышел), снимаем загрузку
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = UiState(
                    error = e.message ?: "Ошибка загрузки",
                    isLoading = false,
                    tasks = emptyList()
                )
                Log.e("TaskViewModel", "❌ Ошибка в loadTasks: ${e.message}")
            }
        }
    }




    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}