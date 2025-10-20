package com.example.cpen_321.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.cpen_321.data.network.api.UserApi
import com.example.cpen_321.data.repository.AuthRepository
import com.example.cpen_321.data.repository.AuthRepositoryImpl
import com.example.cpen_321.data.repository.MatchRepository
import com.example.cpen_321.data.repository.MatchRepositoryImpl
import com.example.cpen_321.data.repository.UserRepository
import com.example.cpen_321.data.repository.UserRepositoryImpl
import dagger.Binds

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {


    @Provides
    @Singleton
    fun provideAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository {
        return authRepositoryImpl
    }

    // Match Repository
    @Provides
    @Singleton
    fun provideMatchRepository(userApi: UserApi): MatchRepository =
        MatchRepositoryImpl(userApi)

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

}





