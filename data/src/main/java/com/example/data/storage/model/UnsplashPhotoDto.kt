package com.example.data.storage.model

data class UnsplashPhotoDto(
    val id: String, // Теперь мы получаем ID напрямую
    val urls: Urls
) {
    data class Urls(
        val small: String,
        val regular: String // Теперь мы получаем regular URL
    )
}
