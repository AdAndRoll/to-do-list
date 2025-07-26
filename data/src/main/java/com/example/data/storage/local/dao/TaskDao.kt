package com.example.data.storage.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.data.storage.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface TaskDao {
    // Сортируем сначала локальные, затем по времени создания (новые первыми)
    @Query("SELECT * FROM tasks ORDER BY isLocalOnly DESC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Заменяет, если PK совпадает
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    @Query("DELETE FROM tasks WHERE isLocalOnly = 0") // Удаляем только синхронизированные (не локальные) задачи
    suspend fun deleteAllNonLocalTasks()
}