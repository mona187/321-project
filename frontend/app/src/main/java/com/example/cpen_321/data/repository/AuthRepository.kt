package com.example.cpen_321.data.repository

import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.network.dto.AuthResponse
import com.example.cpen_321.data.network.dto.AuthUser

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {

    /**
     * Authenticate with Google ID token
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