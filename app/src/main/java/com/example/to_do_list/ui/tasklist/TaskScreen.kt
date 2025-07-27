package com.example.to_do_list.ui.tasklist

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.domain.model.Task // <-- Необходим импорт Task
import com.example.domain.model.UnsplashPhoto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TaskScreen(viewModel: TaskViewModel = hiltViewModel()) {
    // Состояния из ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unsplashPhotos by viewModel.unsplashPhotos.collectAsStateWithLifecycle()
    val isPhotosLoading by viewModel.isPhotosLoading.collectAsStateWithLifecycle()

    // Состояния для управления UI
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    // Состояния для новой задачи
    var newTaskTitle by remember { mutableStateOf("") }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }

    // <-- НОВЫЕ СОСТОЯНИЯ ДЛЯ РЕДАКТИРОВАНИЯ
    var taskToEdit: Task? by remember { mutableStateOf(null) } // Задача, которую сейчас редактируем
    var showEditTaskDialog by remember { mutableStateOf(false) } // Флаг для отображения диалога редактирования
    // <-- КОНЕЦ НОВЫХ СОСТОЯНИЙ

    // Отображение Snackbar при ошибках
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.tasks) {
        if (uiState.tasks.isNotEmpty()) {
            // Прокручиваем к началу, если список не пуст
            lazyListState.animateScrollToItem(0)
        }
    }

    // Диалог выбора картинок Unsplash (для добавления новой задачи)
    if (showImagePickerDialog) {
        UnsplashImagePicker(
            photos = unsplashPhotos,
            isLoading = isPhotosLoading,
            onPhotoSelected = { photoUrl ->
                selectedImageUrl = photoUrl // Сохраняем выбранный URL
                Log.d("TaskScreen", "Выбрана картинка: $photoUrl")
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

    // <-- НОВЫЙ ДИАЛОГ РЕДАКТИРОВАНИЯ ЗАДАЧИ
    if (showEditTaskDialog && taskToEdit != null) {
        EditTaskDialog(
            taskToEdit = taskToEdit!!, // Передаем задачу для редактирования
            unsplashPhotos = unsplashPhotos, // Список фото для выбора
            isPhotosLoading = isPhotosLoading, // Статус загрузки фото
            onSaveEdit = { updatedTask ->
                viewModel.saveEditedTask(updatedTask) // Вызываем метод ViewModel для сохранения
                taskToEdit = null // Сбрасываем задачу для редактирования
                showEditTaskDialog = false // Закрываем диалог
                viewModel.clearUnsplashPhotos() // Очищаем фото после выбора
            },
            onDismiss = {
                taskToEdit = null // Сбрасываем задачу
                showEditTaskDialog = false // Закрываем диалог
                viewModel.clearUnsplashPhotos() // Очищаем фото при отмене
            },
            onLoadMorePhotos = { viewModel.loadUnsplashPhotos() }, // Загрузка еще фото
            onSelectNewPhoto = { photoUrl ->
                // Возможно, здесь можно как-то обработать выбор фото, если нужно
                // для прямого отображения в UI TaskScreen, но диалог EditTaskDialog
                // уже сам обновляет свое внутреннее состояние editedImageUrl.
            }
        )
    }
    // <-- КОНЕЦ НОВОГО ДИАЛОГА

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Мои Задачи") },
                actions = {
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
            // Блок для добавления новой задачи (без изменений)
            OutlinedTextField(
                value = newTaskTitle,
                onValueChange = { newTaskTitle = it },
                label = { Text("Заголовок задачи") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    showImagePickerDialog = true
                    viewModel.loadUnsplashPhotos()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Выбрать картинку")
            }

            selectedImageUrl?.let { url ->
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = url,
                    contentDescription = "Выбранная картинка для задачи",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        viewModel.addNewTask(
                            title = newTaskTitle,
                            imageUrl = selectedImageUrl
                        )
                        newTaskTitle = "" // Очищаем поле ввода
                        selectedImageUrl = null // Сбрасываем выбранную картинку
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Заголовок задачи не может быть пустым")
                        }
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Добавить задачу")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Отображение списка задач (с изменениями для редактирования)
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
                            text = "Задач нет. Добавьте новую или обновите.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(uiState.tasks, key = { it.id }) { task ->
                            TaskItem(
                                task = task,
                                onToggleStatus = { clickedTask ->
                                    viewModel.toggleTaskStatus(clickedTask)
                                },
                                onEditTask = { taskToEditPassed -> // <-- НОВАЯ ЛЯМБДА ВЫЗЫВАЕТ ДИАЛОГ
                                    taskToEdit = taskToEditPassed // Сохраняем задачу, которую хотим редактировать
                                    showEditTaskDialog = true      // Открываем диалог редактирования
                                    viewModel.loadUnsplashPhotos() // Загружаем фото для picker'а
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}