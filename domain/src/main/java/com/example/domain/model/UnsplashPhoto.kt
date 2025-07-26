package com.example.domain.model

data class UnsplashPhoto(
    val id: String, // ID фото (используем как уникальный ключ для списка)
    val regularUrl: String, // URL для отображения (побольше)
    val smallUrl: String // URL для использования в превью или в качестве imageUrl для Task
)