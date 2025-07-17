package com.example.to_do_list.ui.state

import com.example.domain.model.Task

data class UiState(
    val tasks: List<Task> = emptyList(),
    val error: String? = null,
    val isLoading: Boolean = false
)