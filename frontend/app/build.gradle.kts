import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
    id("com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { stream ->
            load(stream)
        }
    }
}

fun getLocalProperty(key: String, defaultValue: String = ""): String {
    return localProperties.getProperty(key) ?: System.getenv(key) ?: defaultValue
}

android {
    namespace = "com.example.cpen_321"
    compileSdk = 36  // UPDATED from 34 to 35 to satisfy dependency requirements

    signingConfigs {
        create("release") {
            val keystorePath = getLocalProperty("RELEASE_KEYSTORE_PATH", "")
            if (keystorePath.isNotEmpty()) {
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
            val hasKeystore = signingConfigs.findByName("release") != null
            if (hasKeystore) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                println("No release keystore found. If this is not on the GitHub Action we have an issue.")
            }
//            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isTestCoverageEnabled = true
            isDebuggable = true
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    testOptions {
        animationsDisabled = true
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/NOTICE.md"
        }
    }

    sourceSets {
        getByName("androidTest") {
            java.srcDirs("src/androidTest/java", "src/androidTest/kotlin")
        }
    }
}

// Force dependency resolution strategy
configurations.all {
    resolutionStrategy {
        force("androidx.test:runner:1.5.2")
        force("androidx.test.ext:junit:1.1.5")
        force("androidx.test.espresso:espresso-core:3.5.1")
        force("androidx.compose.ui:ui-test-junit4:1.8.2")
    }
}

dependencies {
    // Core app dependencies
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.7")
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
    implementation("io.socket:socket.io-client:2.1.0")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Google Sign-In & Credentials
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Compose - Using BOM for version alignment
    val composeBom = platform("androidx.compose:compose-bom:2024.11.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // AndroidX & Compose
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // ========== TESTING DEPENDENCIES ==========

    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")

    // Android Instrumented Testing - Core
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Compose Testing - Explicitly add all needed dependencies
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // UI Automator for system-level interactions
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")

    // Hilt Testing
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.57.2")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.57.2")

    // Coroutines Testing
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Additional Testing Support
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
}

kapt {
    correctErrorTypes = true
}

// Test Tasks
tasks.register("runE2ETests") {
    description = "Run all E2E tests"
    dependsOn("connectedAndroidTest")
}

tasks.register("runE2ETestsWithCoverage") {
    description = "Run E2E tests with coverage report"
    dependsOn("createDebugCoverageReport")
}