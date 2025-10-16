package com.example.cpen_321

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.jakewharton.threetenabp.AndroidThreeTen

@HiltAndroidApp
class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this) // this is needed to implement timer in waiting room
    }
}