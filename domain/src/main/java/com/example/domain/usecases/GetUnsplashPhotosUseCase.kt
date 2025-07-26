package com.example.domain.usecases

import com.example.domain.repository.TaskRepository // Ваш репозиторий уже умеет получать фото
import com.example.domain.model.UnsplashPhoto // Мы создадим эту модель ниже

class GetUnsplashPhotosUseCase(private val taskRepository: TaskRepository) {
    suspend fun execute(count: Int = 30): List<UnsplashPhoto> { // Можно указать количество фото для загрузки
        // Этот метод будет вызывать fetchRandomPhotos в репозитории, но теперь он вернет доменную модель.
        return taskRepository.getUnsplashPhotos(count)
    }
}