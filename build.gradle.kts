plugins {
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application) apply false
    id("com.google.devtools.ksp") version "2.3.2" apply false
}