package com.example.cpen_321.data.network.api

import com.example.cpen_321.data.network.dto.AuthResponse
import com.example.cpen_321.data.network.dto.FcmTokenRequest
import com.example.cpen_321.data.network.dto.GoogleAuthRequest
import com.example.cpen_321.data.network.dto.MessageResponse
import com.example.cpen_321.data.network.dto.SignUpResponse
import com.example.cpen_321.data.network.dto.TokenVerifyResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Authentication API endpoints
 */
interface AuthAPI {

    /**
     * POST /api/auth/signup
     * Sign up with Google (create new account)
     * Note: Returns SignUpResponse (no token) - user must sign in separately
     */
    @POST("api/auth/signup")
    suspend fun signUp(
        @Body request: GoogleAuthRequest
    ): Response<SignUpResponse>

    /**
     * POST /api/auth/signin
     * Sign in with Google (existing account)
     */
    @POST("api/auth/signin")
    suspend fun signIn(
        @Body request: GoogleAuthRequest
    ): Response<AuthResponse>

    /**
     * POST /api/auth/google
     * Exchange Google ID token for JWT (legacy - find or create)
     */
    @POST("api/auth/google")
    suspend fun googleAuth(
        @Body request: GoogleAuthRequest
    ): Response<AuthResponse>

    /**
     * POST /api/auth/logout
     * Logout user (set status to offline)
     */
    @POST("api/auth/logout")
    suspend fun logout(): Response<MessageResponse>

    /**
     * GET /api/auth/verify
     * Verify JWT token and return user info
     */
    @GET("api/auth/verify")
    suspend fun verifyToken(): Response<TokenVerifyResponse>

    /**
     * POST /api/auth/fcm-token
     * Update user's FCM token for push notifications
     */
    @POST("api/auth/fcm-token")
    suspend fun updateFcmToken(
        @Body request: FcmTokenRequest
    ): Response<MessageResponse>

    /**
     * DELETE /api/auth/account
     * Delete user account
     */
    @DELETE("api/auth/account")
    suspend fun deleteAccount(): Response<MessageResponse>
}