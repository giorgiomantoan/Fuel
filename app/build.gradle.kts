import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

// Carica local.properties (NON versionato) per leggere la chiave API Thunderforest.
// La chiave non viene committata: va aggiunta manualmente in local.properties
// con la riga:  FUEL_THUNDERFOREST_API_KEY=la_tua_chiave
val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.example.fuel"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fuel"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.2.0"

        // ABI supportate: ARM reale dei telefoni moderni (es. Xiaomi Poco X4 5G,
        // Snapdragon 695 -> arm64-v8a). Escludiamo x86/x86_64 (solo emulatori)
        // per ridurre la dimensione dell'APK a parità di compatibilità.
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }

        // Room schema export
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }

        // Chiave API Thunderforest letta da local.properties / gradle.properties / variabile d'ambiente.
        // La chiave NON è committata: senza di essa l'app compila ma la mappa resta vuota.
        // Vedi README.md -> "Configurazione della chiave Thunderforest".
        val thunderforestApiKey = providers.gradleProperty("FUEL_THUNDERFOREST_API_KEY").getOrNull()
            ?: localProperties.getProperty("FUEL_THUNDERFOREST_API_KEY")
            ?: System.getenv("FUEL_THUNDERFOREST_API_KEY")
            ?: ""
        buildConfigField(
            "String",
            "THUNDERFOREST_API_KEY",
            "\"$thunderforestApiKey\""
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        abortOnError = false
    }

    // Configurazione firma release: evita credenziali hardcoded e build rotte
    // se il keystore non è presente. Percorso e password da gradle.properties o env.
    // Esempio gradle.properties:
    //   FUEL_KEYSTORE_PATH=release.keystore
    //   FUEL_KEYSTORE_PASSWORD=...
    //   FUEL_KEY_ALIAS=fuel
    //   FUEL_KEY_PASSWORD=...
    val releaseKeystore = file(providers.gradleProperty("FUEL_KEYSTORE_PATH").orNull ?: "release.keystore")
    val hasReleaseKeystore = releaseKeystore.exists()

    signingConfigs {
        create("release") {
            if (hasReleaseKeystore) {
                storeFile = releaseKeystore
                storePassword = providers.gradleProperty("FUEL_KEYSTORE_PASSWORD").orNull ?: ""
                keyAlias = providers.gradleProperty("FUEL_KEY_ALIAS").orNull ?: "fuel"
                keyPassword = providers.gradleProperty("FUEL_KEY_PASSWORD").orNull ?: ""
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.9.3")

    // Lifecycle + ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // OkHttp (download CSV)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // OSMDroid (mappe OpenStreetMap)
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // Google Play Services (solo per location)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Core
    implementation("androidx.core:core-ktx:1.15.0")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
