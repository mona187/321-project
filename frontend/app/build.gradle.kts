import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
    id("com.google.gms.google-services")
}

// 👇 ADD THIS SECTION - Read from local.properties 👇
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { stream ->
            load(stream)
        }
    }
}

// Helper function to get property with fallback
fun getLocalProperty(key: String, defaultValue: String = ""): String {
    return localProperties.getProperty(key) ?: System.getenv(key) ?: defaultValue
}

android {
    namespace = "com.example.cpen_321"
    compileSdk = 36

    // 👇 ADD SIGNING CONFIGS HERE 👇
    signingConfigs {
        create("release") {
            val keystorePath = getLocalProperty("RELEASE_KEYSTORE_PATH", "")
            if (keystorePath.isNotEmpty()) {
                // Use rootProject.file() to handle paths correctly
                storeFile = rootProject.file(keystorePath)
                storePassword = getLocalProperty("RELEASE_KEYSTORE_PASSWORD", "")
                keyAlias = getLocalProperty("RELEASE_KEY_ALIAS", "")
                keyPassword = getLocalProperty("RELEASE_KEY_PASSWORD", "")
            }
        }
    }

    defaultConfig {
        applicationId = "com.example.cpen_321"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.example.cpen_321.HiltTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "false"

        // 👇 CHANGED - Now reads from local.properties 👇
        buildConfigField(
            "String",
            "GOOGLE_CLIENT_ID",
            "\"${getLocalProperty("GOOGLE_CLIENT_ID", "1066689966317-k0pgjdvova4h643qnsudps6hnelmvm61.apps.googleusercontent.com")}\""
        )
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"${getLocalProperty("API_BASE_URL", "http://3.135.231.73:3000/")}\""
        )
        buildConfigField(
            "String",
            "IMAGE_BASE_URL",
            "\"${getLocalProperty("IMAGE_BASE_URL", "http://3.135.231.73:3000/")}\""
        )
    }

    buildTypes {
        release {
            // 👇 ADD THIS LINE 👇
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets {
        getByName("androidTest") {
            java.srcDirs("src/androidTest/java")
        }
    }

    // 👇 NEW - Test Options for E2E Testing 👇
    testOptions {
        animationsDisabled = true
        unitTests.isReturnDefaultValues = true
    }

    // 👇 NEW - Packaging Options 👇
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"      // ADD THIS
            excludes += "META-INF/LICENSE-notice.md" // ADD THIS
            excludes += "META-INF/NOTICE.md"       // ADD THIS
        }
    }
}

dependencies {
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.7")
    implementation(platform("androidx.compose:compose-bom:2025.10.00"))
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.datastore:datastore-core:1.1.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.google.dagger:hilt-android:2.57.2")
    kapt("com.google.dagger:hilt-compiler:2.57.2")

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Socket.IO client
    implementation("io.socket:socket.io-client:2.1.2")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Google Sign-In & Credentials
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")
    implementation("androidx.credentials:credentials:1.2.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

    // AndroidX & Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // ========== TESTING DEPENDENCIES ==========
    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")

    // Android Instrumented Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Compose Testing
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // UI Automator for system-level interactions
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")

    // Hilt Testing
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48.1")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.48.1")

    // Coroutines Testing
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // MockK for Android Tests (optional)
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
//    // Unit Testing
//    testImplementation(libs.junit)
//    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
//
//    // Android Instrumentation Testing - Core
//    androidTestImplementation(libs.androidx.junit)
//
//    // Force Espresso 3.5.0 (Compose requires this version)
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0") {
//        version {
//            strictly("3.5.0")
//        }
//    }
//
//    androidTestImplementation("androidx.test:core:1.5.0")
//    androidTestImplementation("androidx.test:core-ktx:1.5.0")
//    androidTestImplementation("androidx.test:runner:1.5.0")
//    androidTestImplementation("androidx.test:rules:1.5.0")
//
//    // UI Automator for cross-app E2E testing
//    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
//
//    // Compose Testing
//    androidTestImplementation(platform(libs.androidx.compose.bom))
//    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
//    debugImplementation(libs.androidx.compose.ui.tooling)
//    debugImplementation(libs.androidx.compose.ui.test.manifest)
//
//    // Hilt Testing
//    androidTestImplementation("com.google.dagger:hilt-android-testing:2.57.2")
//    kaptAndroidTest("com.google.dagger:hilt-compiler:2.57.2")
//
//    // Coroutines Testing
//    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
//
//    // Navigation Testing
//    androidTestImplementation("androidx.navigation:navigation-testing:2.8.0")
}

// 👇 NEW - Kapt Configuration 👇
kapt {
    correctErrorTypes = true
}