package com.example.cpen_321.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_GOOGLE_ID = "google_id"
        private const val KEY_PROFILE_PICTURE = "profile_picture"

        @Volatile
        private var INSTANCE: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Save JWT token
     */
    fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
    }

    /**
     * Get JWT token
     */
    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    /**
     * Save user information
     */
    fun saveUserInfo(userId: String, email: String, googleId: String, profilePicture: String? = null) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_EMAIL, email)
            putString(KEY_GOOGLE_ID, googleId)
            putString(KEY_PROFILE_PICTURE, profilePicture)
            apply()
        }
    }

    /**
     * Get user ID
     */
    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }

    /**
     * Get email
     */
    fun getEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }

    /**
     * Get profile picture
     */
    fun getProfilePicture(): String? {
        return sharedPreferences.getString(KEY_PROFILE_PICTURE, null)
    }

    /**
     * Get Google ID
     */
    fun getGoogleId(): String? {
        return sharedPreferences.getString(KEY_GOOGLE_ID, null)
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    /**
     * Clear all stored data (logout)
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Clear only token (for token refresh scenarios)
     */
    fun clearToken() {
        sharedPreferences.edit().remove(KEY_TOKEN).apply()
    }
}