plugins {
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application) apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}