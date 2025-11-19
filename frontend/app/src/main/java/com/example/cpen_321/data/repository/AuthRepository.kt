package com.example.cpen_321.data.repository

import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.network.dto.AuthResponse
import com.example.cpen_321.data.network.dto.AuthUser
import com.example.cpen_321.data.network.dto.SignUpResponse

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {

    /**
     * Sign up with Google ID token (create new account)
     * Note: Does not return a token - user must sign in separately
     */
    suspend fun signUp(idToken: String): ApiResult<SignUpResponse>

    /**
     * Sign in with Google ID token (existing account)
     */
    suspend fun signIn(idToken: String): ApiResult<AuthResponse>

    /**
     * Authenticate with Google ID token (legacy - find or create)
     */
    suspend fun googleAuth(idToken: String): ApiResult<AuthResponse>

    /**
     * Logout user
     */
    suspend fun logout(): ApiResult<String>

    /**
     * Verify JWT token
     */
    suspend fun verifyToken(): ApiResult<AuthUser>

    /**
     * Update FCM token for push notifications
     */
    suspend fun updateFcmToken(fcmToken: String): ApiResult<String>

    /**
     * Delete user account
     */
    suspend fun deleteAccount(): ApiResult<String>

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String?

    /**
     * Clear all auth data (local logout)
     */
    fun clearAuthData()
}