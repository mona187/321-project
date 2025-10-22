package com.example.cpen_321.di

import com.example.cpen_321.data.local.PreferencesManager
import com.example.cpen_321.data.local.TokenManager
import com.example.cpen_321.data.repository.AuthRepository
import com.example.cpen_321.data.repository.AuthRepositoryImpl
import com.example.cpen_321.data.repository.GroupRepository
import com.example.cpen_321.data.repository.GroupRepositoryImpl
import com.example.cpen_321.data.repository.MatchRepository
import com.example.cpen_321.data.repository.MatchRepositoryImpl
import com.example.cpen_321.data.repository.RestaurantRepository
import com.example.cpen_321.data.repository.RestaurantRepositoryImpl
import com.example.cpen_321.data.repository.UserRepository
import com.example.cpen_321.data.repository.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provide AuthRepository
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        tokenManager: TokenManager,
        preferencesManager: PreferencesManager
    ): AuthRepository {
        return AuthRepositoryImpl(tokenManager, preferencesManager)
    }

    /**
     * Provide UserRepository
     */
    @Provides
    @Singleton
    fun provideUserRepository(
        preferencesManager: PreferencesManager
    ): UserRepository {
        return UserRepositoryImpl(preferencesManager)
    }

    /**
     * Provide MatchRepository
     */
    @Provides
    @Singleton
    fun provideMatchRepository(
        preferencesManager: PreferencesManager
    ): MatchRepository {
        return MatchRepositoryImpl(preferencesManager)
    }

    /**
     * Provide GroupRepository
     */
    @Provides
    @Singleton
    fun provideGroupRepository(
        preferencesManager: PreferencesManager
    ): GroupRepository {
        return GroupRepositoryImpl(preferencesManager)
    }

    /**
     * Provide RestaurantRepository
     */
    @Provides
    @Singleton
    fun provideRestaurantRepository(): RestaurantRepository {
        return RestaurantRepositoryImpl()
    }
}