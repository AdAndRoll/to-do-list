// ui/tasklist/EditTaskDialog.kt
package com.example.to_do_list.ui.tasklist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.domain.model.Task
import com.example.domain.model.UnsplashPhoto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    taskToEdit: Task, // Задача, которую редактируем
    unsplashPhotos: List<UnsplashPhoto>, // Фото для выбора (из ViewModel)
    isPhotosLoading: Boolean, // Состояние загрузки фото
    onSaveEdit: (Task) -> Unit, // Лямбда для сохранения изменений
    onDismiss: () -> Unit, // Лямбда для закрытия диалога без сохранения
    onLoadMorePhotos: () -> Unit, // Лямбда для загрузки новых фото Unsplash
    onSelectNewPhoto: (String?) -> Unit // Лямбда для выбора новой картинки
) {
    var editedTitle by remember { mutableStateOf(taskToEdit.title) }
    // Используем taskToEdit.imageUrl как начальное значение, если есть
    var editedImageUrl by remember { mutableStateOf(taskToEdit.imageUrl) }
    var showImagePickerDialogForEdit by remember { mutableStateOf(false) } // Внутреннее состояние для picker'а

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Редактировать задачу",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    label = { Text("Заголовок задачи") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onLoadMorePhotos() // Загружаем фото при открытии picker'а
                        showImagePickerDialogForEdit = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Выбрать новую картинку")
                }
                Spacer(modifier = Modifier.height(8.dp))

                editedImageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Выбранная картинка",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(onClick = onDismiss) {
                        Text("Отмена")
                    }
                    Button(
                        onClick = {
                            if (editedTitle.isNotBlank()) {
                                // Создаем обновленную копию задачи
                                val updatedTask = taskToEdit.copy(
                                    title = editedTitle,
                                    imageUrl = editedImageUrl
                                )
                                onSaveEdit(updatedTask) // Сохраняем изменения
                            }
                        },
                        enabled = editedTitle.isNotBlank() // Кнопка активна, если заголовок не пустой
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }

    // Вложенный диалог для выбора Unsplash фото
    if (showImagePickerDialogForEdit) {
        UnsplashImagePicker(
            photos = unsplashPhotos,
            isLoading = isPhotosLoading,
            onPhotoSelected = { photoUrl ->
                editedImageUrl = photoUrl // Обновляем выбранную картинку для редактируемой задачи
                onSelectNewPhoto(photoUrl) // Дополнительно сообщаем в TaskScreen, если нужно
                showImagePickerDialogForEdit = false
                // Здесь не вызываем clearUnsplashPhotos(), чтобы фотографии оставались доступными
                // если пользователь откроет пикер снова в рамках редактирования одной и той же задачи.
                // Если хотите очищать, то вызовите viewModel.clearUnsplashPhotos() здесь.
            },
            onDismiss = {
                showImagePickerDialogForEdit = false
                // viewModel.clearUnsplashPhotos() // Очистить фото, если нужно при отмене выбора
            },
            onLoadMore = {
                onLoadMorePhotos() // Загрузить еще фото
            }
        )
    }
}