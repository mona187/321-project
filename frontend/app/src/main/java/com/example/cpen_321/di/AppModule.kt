package com.example.cpen_321.di

import android.content.Context
import com.example.cpen_321.data.local.PreferencesManager
import com.example.cpen_321.data.local.TokenManager
import com.example.cpen_321.utils.SocketManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for app-level dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provide TokenManager singleton
     */
    @Provides
    @Singleton
    fun provideTokenManager(
        @ApplicationContext context: Context
    ): TokenManager {
        return TokenManager.getInstance(context)
    }

    /**
     * Provide PreferencesManager singleton
     */
    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager.getInstance(context)
    }

    /**
     * Provide SocketManager singleton
     */
    @Provides
    @Singleton
    fun provideSocketManager(): SocketManager {
        return SocketManager.getInstance()
    }
}