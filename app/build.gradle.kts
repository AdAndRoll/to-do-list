plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.compose) // ✅ Вот это нужно!
    kotlin("kapt")
}

android {
    namespace = "com.example.to_do_list"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.to_do_list"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        compose = true // ✅ Включаем поддержку Compose
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13" // ✅ Укажи подходящую версию для Compose 1.6.4
    }
    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.kotlin.stdlib)
    implementation(libs.coroutines.core)
    implementation(libs.hilt.android)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.ui.tooling.preview.android)
    implementation(libs.androidx.room.runtime.android)
    debugImplementation(libs.androidx.ui.tooling)
    kapt(libs.hilt.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.coil)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation(project(":domain")) // Зависимость от модуля domain
    implementation(project(":data"))   // Зависимость от модуля data
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}