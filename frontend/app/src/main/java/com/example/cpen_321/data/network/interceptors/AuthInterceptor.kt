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
        val request = if (token.isNullOrEmpty()) {
            originalRequest
        } else {
            // Add Authorization header with Bearer token
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }

        // Execute the request
        val response = chain.proceed(request)

        // If we get 401 Unauthorized, clear the token
        // This handles expired or invalid tokens
        if (response.code == 401 && !token.isNullOrEmpty()) {
            tokenManager.clearAll()
        }

        return response
    }
}