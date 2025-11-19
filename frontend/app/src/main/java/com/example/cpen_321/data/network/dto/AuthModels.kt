package com.example.cpen_321.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Google authentication request
 * POST /api/auth/google
 */
data class GoogleAuthRequest(
    @SerializedName("idToken")
    val idToken: String
)

/**
 * Authentication response (for signin)
 */
data class AuthResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("user")
    val user: AuthUser
)

/**
 * Sign up response (no token - user must sign in separately)
 */
data class SignUpResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("user")
    val user: AuthUser
)

/**
 * User data in auth response
 */
data class AuthUser(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("profilePicture")
    val profilePicture: String? = null,

    @SerializedName("credibilityScore")
    val credibilityScore: Double
)

/**
 * FCM token update request
 * POST /api/auth/fcm-token
 */
data class FcmTokenRequest(
    @SerializedName("fcmToken")
    val fcmToken: String
)

/**
 * Token verification response
 * GET /api/auth/verify
 */
data class TokenVerifyResponse(
    @SerializedName("user")
    val user: AuthUser
)

/**
 * Simple message response
 */
data class MessageResponse(
    @SerializedName("message")
    val message: String
)