// di/NetworkModule.kt
package com.example.cpen_321.di

import com.example.cpen_321.data.local.TokenManager
import com.example.cpen_321.data.network.api.AuthApi
import com.example.cpen_321.data.network.api.UserApi
import com.example.cpen_321.data.network.api.RestaurantApi
import com.example.cpen_321.data.network.api.MatchingApi
import com.example.cpen_321.data.network.interceptors.AuthInterceptor

import kotlinx.coroutines.runBlocking
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor,
        tokenManager: TokenManager
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor { 
                runBlocking { tokenManager.getTokenSync() }
            })
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    // ✅ add this so AuthRepositoryImpl can inject it
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    // ✅ keep your Match API provider
    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideRestaurantApi(retrofit: Retrofit): RestaurantApi =
        retrofit.create(RestaurantApi::class.java)

    @Provides
    @Singleton
    fun provideMatchingApi(retrofit: Retrofit): MatchingApi =
        retrofit.create(MatchingApi::class.java)
}






