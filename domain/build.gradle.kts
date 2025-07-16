plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)

}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
dependencies {

    implementation(libs.kotlin.stdlib)
    implementation(libs.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.core) // Для mock-объектов
    testImplementation(libs.mockito.kotlin) // Для Kotlin-совместимости
    testImplementation(libs.kotlinx.coroutines.test) // Для тестирования Coroutines
}