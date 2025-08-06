// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Declaraciones de plugins para subproyectos, con 'apply false'
    // Estos plugins se APLICAN realmente en el build.gradle.kts del módulo 'app'
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.googleServices) apply false
    // Asegúrate de que solo hay una de estas líneas para el plugin de Google Services
}

buildscript {
    repositories {
        google() // Repositorio de Maven de Google
        mavenCentral()
    }
    dependencies {
        // Dependencia del plugin de Android Gradle (AGP)
        classpath("com.android.tools.build:gradle:8.2.0") // O la versión de AGP que estés usando
        // Dependencia del plugin de Google Services para Firebase
        classpath("com.google.gms:google-services:4.4.2") // La versión más reciente que necesites
        // Dependencia del plugin de Kotlin Gradle (¡ACTUALIZADO A 2.0.0!)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
    }
}
