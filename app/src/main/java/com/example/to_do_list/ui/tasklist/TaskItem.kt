package com.example.to_do_list.ui.tasklist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.model.Task


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    task:Task,
    onToggleStatus: (Task) -> Unit,
    onEditTask: (Task) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable( // <-- Используем combinedClickable
                onClick = { onToggleStatus(task) }, // Короткое нажатие переключает статус
                onLongClick = { onEditTask(task) } // Долгое нажатие открывает редактирование
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Чекбокс для отображения и изменения статуса выполнения
        Checkbox(
            checked = task.status,
            onCheckedChange = { _ -> onToggleStatus(task) }, // При изменении чекбокса тоже переключаем статус
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.width(8.dp)) // Небольшой отступ после чекбокса

        AsyncImage(
            model = task.imageUrl.takeIf { !it.isNullOrBlank() }
                ?: "[https://via.placeholder.com/50](https://via.placeholder.com/50)", // Плейсхолдер, если картинки нет
            contentDescription = task.title,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                textDecoration = if (task.status) TextDecoration.LineThrough else null
            )
            Text(
                text = "Статус: ${if (task.status) "Выполнено" else "В процессе"}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (task.status) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

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