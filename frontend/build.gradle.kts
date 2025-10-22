// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    id("com.google.gms.google-services") version "4.4.4" apply false

    // 👇 Updated to 2.51.1 to match your app-level gradle
    id("com.google.dagger.hilt.android") version "2.57.2" apply false
}

// 👇 This buildscript block is actually redundant with the plugin above, you can remove it
// but keeping it won't cause issues
buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.51.1")
    }
}