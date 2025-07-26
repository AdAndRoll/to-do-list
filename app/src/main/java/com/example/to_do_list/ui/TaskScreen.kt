package com.example.to_do_list.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.domain.model.UnsplashPhoto
import com.example.to_do_list.ui.state.UiState
import kotlinx.coroutines.launch // Хотя сейчас не используется, оставим на будущее для snackbar

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TaskScreen(viewModel: TaskViewModel = hiltViewModel()) {
    // Состояния из ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val unsplashPhotos by viewModel.unsplashPhotos.collectAsState()
    val isPhotosLoading by viewModel.isPhotosLoading.collectAsState()

    // Состояния для управления UI
    val snackbarHostState = remember { SnackbarHostState() }
    // Удалены: newTaskTitle, selectedImageUrl (так как нет функции добавления задачи)
    var showImagePickerDialog by remember { mutableStateOf(false) } // Для управления видимостью диалога выбора картинок

    // Отображение Snackbar при ошибках
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // Диалог выбора картинок Unsplash
    if (showImagePickerDialog) {
        UnsplashImagePicker(
            photos = unsplashPhotos,
            isLoading = isPhotosLoading,
            onPhotoSelected = { photoUrl ->
                // ВАЖНО: Сейчас выбранный URL никуда не сохраняется в TaskScreen,
                // так как мы убрали addNewTask и связанное с ним поле.
                // В будущем, этот photoUrl будет передаваться в addNewTask ViewModel.
                Log.d("TaskScreen", "Выбрана картинка: $photoUrl (пока не используется для задачи)")
                showImagePickerDialog = false
                viewModel.clearUnsplashPhotos()
            },
            onDismiss = {
                showImagePickerDialog = false
                viewModel.clearUnsplashPhotos()
            },
            onLoadMore = {
                viewModel.loadUnsplashPhotos()
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Мои Задачи") },
                actions = {
                    // Кнопка для открытия диалога выбора картинки (пока здесь, можно перенести)
                    Button(
                        onClick = {
                            showImagePickerDialog = true
                            viewModel.loadUnsplashPhotos()
                        },
                        // Возможно, стоит добавить модификатор для отступа, если она будет всегда в TopAppBar
                    ) {
                        Text("Выбрать фото")
                    }
                    Spacer(modifier = Modifier.width(8.dp)) // Отступ между кнопками
                    IconButton(onClick = { viewModel.refreshTasksFromNetwork() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить задачи")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Отображение списка задач
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.tasks.isEmpty() && !uiState.isLoading && uiState.error == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Задач нет. Нажмите 'Обновить' для загрузки.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(uiState.tasks, key = { it.id }) { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = task.imageUrl.takeIf { !it.isNullOrBlank() }
                                        ?: "https://via.placeholder.com/50",
                                    contentDescription = task.title,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = task.title, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "Статус: ${if (task.status) "Выполнено" else "В процессе"}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (task.isLocalOnly) {
                                        Text(
                                            text = "(Локальная)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Композуемый UnsplashImagePicker остается без изменений
@Composable
fun UnsplashImagePicker(
    photos: List<UnsplashPhoto>,
    isLoading: Boolean,
    onPhotoSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onLoadMore: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите картинку из Unsplash") },
        text = {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading && photos.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    photos.isEmpty() && !isLoading -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Не удалось загрузить изображения.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onLoadMore) {
                                Text("Повторить")
                            }
                        }
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(photos, key = { it.id }) { photo ->
                                AsyncImage(
                                    model = photo.smallUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { onPhotoSelected(photo.regularUrl) },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}