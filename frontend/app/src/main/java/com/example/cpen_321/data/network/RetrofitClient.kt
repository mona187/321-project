package com.example.cpen_321.data.network

import com.example.cpen_321.data.local.TokenManager
import com.example.cpen_321.data.network.api.AuthAPI
import com.example.cpen_321.data.network.api.GroupAPI
import com.example.cpen_321.data.network.api.MatchAPI
import com.example.cpen_321.data.network.api.RestaurantAPI
import com.example.cpen_321.data.network.api.UserAPI
import com.example.cpen_321.data.network.interceptors.AuthInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client singleton for API calls
 */
object RetrofitClient {

    // TODO: Replace with your actual backend URL
    private const val BASE_URL = "http://10.0.2.2:3000/" // Android emulator localhost
    // For physical device, use: "http://YOUR_COMPUTER_IP:3000/"
    // For production: "https://your-backend-domain.com/"

    private var tokenManager: TokenManager? = null

    /**
     * Initialize with TokenManager
     */
    fun initialize(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }

    /**
     * Gson instance with custom configurations
     */
    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .serializeNulls()
            .create()
    }

    /**
     * Logging interceptor for debugging
     */
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * OkHttp client with interceptors
     */
    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)

        // Add auth interceptor if tokenManager is initialized
        tokenManager?.let { tm ->
            builder.addInterceptor(AuthInterceptor(tm))
        }

        builder.build()
    }

    /**
     * Retrofit instance
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Auth API service
     */
    val authAPI: AuthAPI by lazy {
        retrofit.create(AuthAPI::class.java)
    }

    /**
     * User API service
     */
    val userAPI: UserAPI by lazy {
        retrofit.create(UserAPI::class.java)
    }

    /**
     * Match/Room API service
     */
    val matchAPI: MatchAPI by lazy {
        retrofit.create(MatchAPI::class.java)
    }

    /**
     * Group API service
     */
    val groupAPI: GroupAPI by lazy {
        retrofit.create(GroupAPI::class.java)
    }

    /**
     * Restaurant API service
     */
    val restaurantAPI: RestaurantAPI by lazy {
        retrofit.create(RestaurantAPI::class.java)
    }

    /**
     * Update base URL (for switching between environments)
     */
    fun updateBaseUrl(newBaseUrl: String): RetrofitClient {
        // Note: In production, you'd want to recreate the Retrofit instance
        // This is a simplified version
        return this
    }
}