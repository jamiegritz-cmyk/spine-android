plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val ksFile = file("graiz-release.jks")
if (!ksFile.exists()) {
    try {
        ProcessBuilder(
            "keytool", "-genkey", "-v",
            "-keystore", ksFile.absolutePath,
            "-alias", "graiz",
            "-keyalg", "RSA",
            "-keysize", "2048",
            "-validity", "10000",
            "-storepass", "graizmusicplayer",
            "-keypass", "graizmusicplayer",
            "-dname", "CN=Graiz, OU=Graiz, O=Graiz, L=London, ST=London, C=GB"
        ).start().waitFor()
    } catch (e: Exception) {
        println("Could not run keytool: ${e.message}")
    }
}

android {
    namespace = "com.spine.musicplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.spine.musicplayer"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            if (ksFile.exists()) {
                storeFile = ksFile
                storePassword = "graizmusicplayer"
                keyAlias = "graiz"
                keyPassword = "graizmusicplayer"
            } else {
                initWith(getByName("debug"))
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core Android & Lifecycle
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Jetpack Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Media3 / ExoPlayer for foreground local audio playback
    implementation("androidx.media3:media3-exoplayer:1.5.0")
    implementation("androidx.media3:media3-session:1.5.0")
    implementation("androidx.media3:media3-ui:1.5.0")
    implementation("androidx.media3:media3-common:1.5.0")
    implementation("com.google.guava:guava:33.3.1-android")

    // Coil for album artwork loading from MediaStore content URIs
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Accompanist / Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
}

tasks.named("assembleDebug") {
    dependsOn("bundleRelease")
    doLast {
        val aabFile = file("build/outputs/bundle/release/app-release.aab")
        val apkFile = file("build/outputs/apk/debug/app-debug.apk")
        if (aabFile.exists() && apkFile.exists()) {
            try {
                val tempDir = file("build/tmp/aab_embed")
                val assetDir = file("build/tmp/aab_embed/assets")
                assetDir.mkdirs()
                val targetAab = file("build/tmp/aab_embed/assets/Graiz-release.aab")
                aabFile.copyTo(targetAab, overwrite = true)
                ProcessBuilder("zip", "-u", "-r", apkFile.absolutePath, "assets/Graiz-release.aab")
                    .directory(tempDir)
                    .start()
                    .waitFor()
                println("SUCCESS: Embedded assets/Graiz-release.aab into app-debug.apk!")
            } catch (e: Exception) {
                println("Note: could not embed AAB: ${e.message}")
            }
        }
    }
}
