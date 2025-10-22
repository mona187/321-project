package com.example.cpen_321.data.network.dto

import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.model.UserSettings
import com.google.gson.annotations.SerializedName

/**
 * Update user profile request
 * POST/PUT /api/user/profile
 */
data class UpdateProfileRequest(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("bio")
    val bio: String? = null,

    @SerializedName("profilePicture")
    val profilePicture: String? = null,

    @SerializedName("contactNumber")
    val contactNumber: String? = null
)

/**
 * Update user settings request
 * POST /api/user/settings
 */
data class UpdateSettingsRequest(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("bio")
    val bio: String? = null,

    @SerializedName("preference")
    val preference: List<String>? = null,

    @SerializedName("profilePicture")
    val profilePicture: String? = null,

    @SerializedName("contactNumber")
    val contactNumber: String? = null,

    @SerializedName("budget")
    val budget: Double? = null,

    @SerializedName("radiusKm")
    val radiusKm: Double? = null
)

/**
 * User profiles response (for GET /api/user/profile/:ids)
 */
data class UserProfilesResponse(
    @SerializedName("profiles")
    val profiles: List<UserProfile>
)

/**
 * Delete user response
 */
data class DeleteUserResponse(
    @SerializedName("deleted")
    val deleted: Boolean
)