package com.example.data.storage.remote.api

import com.example.data.storage.model.UnsplashPhotoDto
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApi {
    @GET("photos/random")
    suspend fun getRandomPhoto(@Query("client_id") clientId:String): UnsplashPhotoDto
}