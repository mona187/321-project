package com.example.cpen_321

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.jakewharton.threetenabp.AndroidThreeTen
import com.example.cpen_321.data.local.TokenManager
import com.example.cpen_321.data.network.RetrofitClient

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize ThreeTenABP for timer functionality
        AndroidThreeTen.init(this) // this is needed to implement timer in waiting room

        // Initialize RetrofitClient with TokenManager
        val tokenManager = TokenManager.getInstance(this)
        RetrofitClient.initialize(tokenManager)
    }
}