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

    //Room зависимости

    implementation ("androidx.room:room-runtime:2.7.2") // Библиотека "Room"
    kapt ("androidx.room:room-compiler:2.7.2") // Кодогенератор
    implementation ("androidx.room:room-ktx:2.7.2") // Дополнительно для Kotlin Coroutines, Kotlin Flows
    // Зависимость от модуля :domain
    implementation(project(":domain"))

    // Тестовые зависимости
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("org.mockito:mockito-core:5.0.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("junit:junit:4.13.2")
}