package com.example.cpen_321.data.network.interceptors

import com.example.cpen_321.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor to add JWT token to all requests
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Get token from TokenManager
        val token = tokenManager.getToken()

        // If no token, proceed with original request
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Add Authorization header with Bearer token
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}