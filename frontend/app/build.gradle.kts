
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    kotlin("kapt")

//    id("com.android.application")
    id("com.google.gms.google-services")
}
android {
    namespace = "com.example.cpen_321"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.cpen_321"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 👇👇 ADD THIS HERE 👇👇
        buildConfigField(
            "String",
            "GOOGLE_CLIENT_ID",
            "\"419306153379-645ugegg52h4bp07ukmbplq9earqmpfq.apps.googleusercontent.com\""
        )
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"http://10.0.2.2:3000/api/\""
        )

        buildConfigField(
            "String",
            "IMAGE_BASE_URL",
            "\"http://10.0.2.2:3000/api/\""
        )

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
}


dependencies {
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.7")
    implementation(platform("androidx.compose:compose-bom:2025.10.00"))
    implementation ("androidx.compose.material:material-icons-extended")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.datastore:datastore-core:1.1.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation(libs.androidx.espresso.core)
    implementation(libs.googleid)
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("io.socket:socket.io-client:2.1.2") //Socket.IO client

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
}