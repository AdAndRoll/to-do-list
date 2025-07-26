// data/storage/local/entity/TaskEntity.kt
package com.example.data.storage.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) // <-- ВАЖНО: для автогенерации ID
    val id: Int = 0, // Установите 0 для новых задач, Room сгенерирует уникальный ID
    val title: String,
    val completed: Boolean,
    val imageUrl: String?,
    val isLocalOnly: Boolean = false, // True для задач, добавленных только локально
    val isModified: Boolean = false, // Флаг для отслеживания изменений в локальных задачах
    val createdAt: Long // <-- ДОБАВЛЕНО: Время создания задачи в миллисекундах
)