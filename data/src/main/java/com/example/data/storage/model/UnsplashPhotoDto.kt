package com.example.data.storage.model

data class UnsplashPhotoDto(
    val urls: Urls
) {
    data class Urls(
        val small: String
    )
}
