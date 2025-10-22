package com.example.cpen_321.data.repository

import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.model.UserSettings
import com.example.cpen_321.data.network.dto.ApiResult

/**
 * Repository interface for user operations
 */
interface UserRepository {

    /**
     * Get user profiles by IDs
     */
    suspend fun getUserProfiles(userIds: List<String>): ApiResult<List<UserProfile>>

    /**
     * Get current user's settings
     */
    suspend fun getUserSettings(): ApiResult<UserSettings>

    /**
     * Update user profile
     */
    suspend fun updateUserProfile(
        name: String? = null,
        bio: String? = null,
        profilePicture: String? = null,
        contactNumber: String? = null
    ): ApiResult<UserProfile>

    /**
     * Update user settings
     */
    suspend fun updateUserSettings(
        name: String? = null,
        bio: String? = null,
        preference: List<String>? = null,
        profilePicture: String? = null,
        contactNumber: String? = null,
        budget: Double? = null,
        radiusKm: Double? = null
    ): ApiResult<UserSettings>

    /**
     * Delete user account
     */
    suspend fun deleteUser(userId: String): ApiResult<Boolean>
}