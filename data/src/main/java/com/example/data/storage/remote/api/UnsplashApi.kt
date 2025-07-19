package com.example.data.storage.remote.api

import com.example.data.storage.model.UnsplashPhotoDto
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApi {
    @GET("photos/random")
    suspend fun getRandomPhotos(
        @Query("count") count: Int,
        @Query("client_id") clientId: String
    ): List<UnsplashPhotoDto>
}