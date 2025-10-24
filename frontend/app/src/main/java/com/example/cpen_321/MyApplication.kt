package com.example.cpen_321

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.jakewharton.threetenabp.AndroidThreeTen
import com.example.cpen_321.data.local.TokenManager
import com.example.cpen_321.data.network.RetrofitClient
import coil.ImageLoader
import coil.ImageLoaderFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class MyApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()

        // Initialize ThreeTenABP for timer functionality
        AndroidThreeTen.init(this) // this is needed to implement timer in waiting room

        // Initialize RetrofitClient with TokenManager
        val tokenManager = TokenManager.getInstance(this)
        RetrofitClient.initialize(tokenManager)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()
            }
            .build()
    }
}