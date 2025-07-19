plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt") // Следуем вашему стилю из :app
    alias(libs.plugins.hilt) // Для внедрения зависимостей Hilt
}

android {
    namespace = "com.example.to_do_list.data" // Уточняем namespace для соответствия проекту
    compileSdk = 35 // Приводим в соответствие с :app

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        }
    }
}

dependencies {
    // Основные зависимости
    implementation(libs.kotlin.stdlib) // Kotlin стандартная библиотека
    implementation(libs.coroutines.core) // Coroutines для асинхронных операций
    implementation(libs.retrofit) // Retrofit для сетевых запросов
    implementation(libs.retrofit.converter.gson) // Конвертер для JSON
    implementation(libs.okhttp) // OkHttp для сетевых операций
    implementation(libs.hilt.android)
    implementation(libs.androidx.media3.common.ktx) // Hilt для внедрения зависимостей
    kapt(libs.hilt.compiler) // Kapt для обработки аннотаций Hilt

    // Зависимость от модуля :domain
    implementation(project(":domain"))

    // Тестовые зависимости
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.mockito.core) // Для mock-объектов
    testImplementation(libs.mockito.kotlin) // Для Kotlin-совместимости
    testImplementation(libs.kotlinx.coroutines.test) // Для тестирования Coroutines
}