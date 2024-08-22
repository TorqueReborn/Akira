plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}

android {
    namespace = "com.ghostreborn.akira"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ghostreborn.akira"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            viewBinding {
                enable = true
            }
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
    // Zoomable ImageView
    implementation(libs.zoomimage.view.sketch)
    implementation(libs.zoomimage.view.coil)

    // Coil
    implementation(libs.coil)

    // Exoplayer
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)

    // Room Database
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)

    // UI
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
}