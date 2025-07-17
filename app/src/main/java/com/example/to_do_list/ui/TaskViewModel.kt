package com.example.to_do_list.ui


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
            _uiState.value = UiState(isLoading = true)
            try {
                getTasksUseCase.execute().collect { tasks ->
                    _uiState.value = UiState(tasks = tasks, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = UiState(
                    tasks = emptyList(),
                    error = e.message ?: "Failed to load tasks",
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}