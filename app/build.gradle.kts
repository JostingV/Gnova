plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") // Plugin de Google Services
    id("kotlin-kapt") // Para el procesador de anotaciones de Glide (y Room si lo usas)
}

android {
    namespace = "com.proyecto.tienda.gnova" // Asegúrate de que este sea el nombre de tu paquete
    compileSdk = 34 // O la versión de SDK que estés usando

    defaultConfig {
        applicationId = "com.proyecto.tienda.gnova" // Debe coincidir con el nombre del paquete en Firebase
        minSdk = 24 // O la versión mínima de SDK que necesites
        targetSdk = 34 // O la versión de SDK que estés usando
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Dependencias estándar de Android
    implementation("androidx.core:core-ktx:1.13.1") // Versión más reciente
    implementation("androidx.appcompat:appcompat:1.6.1") // Versión más reciente
    implementation("com.google.android.material:material:1.12.0") // Versión más reciente
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // Versión más reciente

    // Navigation Drawer
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    // La dependencia de Material ya está incluida arriba, no es necesario duplicarla si es la misma versión.
    // Si necesitas una versión diferente de Material para DrawerLayout, lo especificas aquí.

    // Firebase BOM (Platform) - Importante para manejar versiones compatibles de Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.15.0")) // Usa la última versión del BOM de Firebase

    // Dependencias de Firebase
    implementation("com.google.firebase:firebase-firestore-ktx") // Firestore
    implementation("com.google.firebase:firebase-analytics-ktx") // Analytics (opcional, si lo usas)
    // Agrega otras dependencias de Firebase si las necesitas (Auth, Storage, etc.)
    // implementation("com.google.firebase:firebase-auth-ktx")

    // Glide para carga de imágenes desde URL
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.firestore)
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Kotlin Coroutines para LiveData y Flow
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") // Para lifecycleScope
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0") // Si usas LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0") // Si usas ViewModel

    // Pruebas
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
