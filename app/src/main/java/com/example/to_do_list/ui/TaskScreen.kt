package com.example.to_do_list.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.to_do_list.ui.state.UiState
import kotlinx.coroutines.launch

@Composable
fun TaskScreen(viewModel: TaskViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Показываем Snackbar при ошибке
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    items(uiState.tasks) { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
//                            AsyncImage(
//                                model = "https://images.unsplash.com/photo-1519681393784-d120267933ba",
//                                contentDescription = "Test Image",
//                                modifier = Modifier.size(50.dp)
//                            )
                            AsyncImage(
                                model = task.imageUrl.takeIf { !it.isNullOrBlank() }
                                    ?: "https://images.unsplash.com/photo-1519681393784-d120267933ba",
                                contentDescription = task.title,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = task.title)
                                Text(
                                    text = "Status: ${
                                        if (task.status) "Completed" else "Pending"
                                    }"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
