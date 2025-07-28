package com.example.data.storage.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

import com.example.data.storage.local.dao.TaskDao
import com.example.data.storage.local.entity.TaskEntity

@Database(entities = [TaskEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

}