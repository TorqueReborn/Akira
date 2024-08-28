plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
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
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
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
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.coil)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
}