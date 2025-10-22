package com.example.cpen_321.di

import com.example.cpen_321.data.local.TokenManager
import com.example.cpen_321.data.network.RetrofitClient
import com.example.cpen_321.data.network.api.AuthAPI
import com.example.cpen_321.data.network.api.GroupAPI
import com.example.cpen_321.data.network.api.MatchAPI
import com.example.cpen_321.data.network.api.RestaurantAPI
import com.example.cpen_321.data.network.api.UserAPI
import com.example.cpen_321.data.network.interceptors.AuthInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module for network-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // TODO: Replace with your actual backend URL
    private const val BASE_URL = "http://10.0.2.2:3000/" // Android emulator localhost
    // For physical device, use: "http://YOUR_COMPUTER_IP:3000/"
    // For production: "https://your-backend-domain.com/"

    /**
     * Provide Gson instance
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .serializeNulls()
            .create()
    }

    /**
     * Provide AuthInterceptor
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    /**
     * Provide HttpLoggingInterceptor
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Provide OkHttpClient
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Provide Retrofit instance
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Provide AuthAPI
     */
    @Provides
    @Singleton
    fun provideAuthAPI(retrofit: Retrofit): AuthAPI {
        return retrofit.create(AuthAPI::class.java)
    }

    /**
     * Provide UserAPI
     */
    @Provides
    @Singleton
    fun provideUserAPI(retrofit: Retrofit): UserAPI {
        return retrofit.create(UserAPI::class.java)
    }

    /**
     * Provide MatchAPI
     */
    @Provides
    @Singleton
    fun provideMatchAPI(retrofit: Retrofit): MatchAPI {
        return retrofit.create(MatchAPI::class.java)
    }

    /**
     * Provide GroupAPI
     */
    @Provides
    @Singleton
    fun provideGroupAPI(retrofit: Retrofit): GroupAPI {
        return retrofit.create(GroupAPI::class.java)
    }

    /**
     * Provide RestaurantAPI
     */
    @Provides
    @Singleton
    fun provideRestaurantAPI(retrofit: Retrofit): RestaurantAPI {
        return retrofit.create(RestaurantAPI::class.java)
    }
}