package com.example.cpen_321.data.model

import com.google.gson.annotations.SerializedName

/**
 * User model matching backend User structure
 */
data class User(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("bio")
    val bio: String? = null,

    @SerializedName("profilePicture")
    val profilePicture: String? = null,

    @SerializedName("preference")
    val preference: List<String> = emptyList(),

    @SerializedName("credibilityScore")
    val credibilityScore: Double = 100.0,

    @SerializedName("contactNumber")
    val contactNumber: String? = null,

    @SerializedName("budget")
    val budget: Double? = 0.0,

    @SerializedName("radiusKm")
    val radiusKm: Double? = 5.0,

    @SerializedName("status")
    val status: UserStatus = UserStatus.OFFLINE,

    @SerializedName("roomID")
    val roomId: String? = null,

    @SerializedName("groupID")
    val groupId: String? = null
)

/**
 * User status enum matching backend
 */
enum class UserStatus(val value: Int) {
    OFFLINE(0),
    ONLINE(1),
    IN_WAITING_ROOM(2),
    IN_GROUP(3);

    companion object {
        fun fromValue(value: Int): UserStatus {
            return entries.find { it.value == value } ?: OFFLINE
        }
    }
}

/**
 * User profile (subset of User for public profiles)
 */
data class UserProfile(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("bio")
    val bio: String? = null,

    @SerializedName("profilePicture")
    val profilePicture: String? = null,

    @SerializedName("contactNumber")
    val contactNumber: String? = null
)

/**
 * User settings (full user data for settings screen)
 */
data class UserSettings(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("bio")
    val bio: String? = null,

    @SerializedName("preference")
    val preference: List<String> = emptyList(),

    @SerializedName("profilePicture")
    val profilePicture: String? = null,

    @SerializedName("credibilityScore")
    val credibilityScore: Double = 100.0,

    @SerializedName("contactNumber")
    val contactNumber: String? = null,

    @SerializedName("budget")
    val budget: Double = 0.0,

    @SerializedName("radiusKm")
    val radiusKm: Double = 5.0,

    @SerializedName("status")
    val status: Int = 0,

    @SerializedName("roomID")
    val roomID: String? = null,

    @SerializedName("groupID")
    val groupID: String? = null
)