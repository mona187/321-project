package com.example.cpen_321.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.cpen_321.data.network.api.MatchApi
import com.example.cpen_321.data.repository.MatchRepository
import com.example.cpen_321.data.repository.MatchRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    // Match Repository
    @Provides
    @Singleton
    fun provideMatchRepository(matchApi: MatchApi): MatchRepository =
        MatchRepositoryImpl(matchApi)

}