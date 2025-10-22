package com.example.cpen_321.data.network.api

import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.model.UserSettings
import com.example.cpen_321.data.network.dto.ApiResponse
import com.example.cpen_321.data.network.dto.DeleteUserResponse
import com.example.cpen_321.data.network.dto.UpdateProfileRequest
import com.example.cpen_321.data.network.dto.UpdateSettingsRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * User API endpoints
 */
interface UserAPI {

    /**
     * GET /api/user/profile/:ids
     * Get user profiles by IDs (comma-separated)
     */
    @GET("api/user/profile/{ids}")
    suspend fun getUserProfiles(
        @Path("ids") ids: String
    ): Response<ApiResponse<List<UserProfile>>>

    /**
     * GET /api/user/settings
     * Get current user's settings
     */
    @GET("api/user/settings")
    suspend fun getUserSettings(): Response<ApiResponse<UserSettings>>

    /**
     * POST /api/user/profile
     * Create/update user profile
     */
    @POST("api/user/profile")
    suspend fun createUserProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<UserProfile>>

    /**
     * POST /api/user/settings
     * Update user settings
     */
    @POST("api/user/settings")
    suspend fun updateUserSettings(
        @Body request: UpdateSettingsRequest
    ): Response<ApiResponse<UserSettings>>

    /**
     * PUT /api/user/profile
     * Update user profile
     */
    @PUT("api/user/profile")
    suspend fun updateUserProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<UserProfile>>

    /**
     * DELETE /api/user/:userId
     * Delete user account
     */
    @DELETE("api/user/{userId}")
    suspend fun deleteUser(
        @Path("userId") userId: String
    ): Response<ApiResponse<DeleteUserResponse>>
}