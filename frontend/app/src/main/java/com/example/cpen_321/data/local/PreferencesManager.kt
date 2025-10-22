package com.example.cpen_321.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "user_preferences"
        private const val KEY_CUISINES = "selected_cuisines"
        private const val KEY_BUDGET = "budget"
        private const val KEY_RADIUS = "radius_km"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_CURRENT_ROOM_ID = "current_room_id"
        private const val KEY_CURRENT_GROUP_ID = "current_group_id"

        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    /**
     * Save selected cuisines
     */
    fun saveCuisines(cuisines: Set<String>) {
        sharedPreferences.edit().putStringSet(KEY_CUISINES, cuisines).apply()
    }

    /**
     * Get selected cuisines
     */
    fun getCuisines(): Set<String> {
        return sharedPreferences.getStringSet(KEY_CUISINES, emptySet()) ?: emptySet()
    }

    /**
     * Save budget
     */
    fun saveBudget(budget: Double) {
        sharedPreferences.edit().putFloat(KEY_BUDGET, budget.toFloat()).apply()
    }

    /**
     * Get budget
     */
    fun getBudget(): Double {
        return sharedPreferences.getFloat(KEY_BUDGET, 50.0f).toDouble()
    }

    /**
     * Save radius in kilometers
     */
    fun saveRadius(radiusKm: Double) {
        sharedPreferences.edit().putFloat(KEY_RADIUS, radiusKm.toFloat()).apply()
    }

    /**
     * Get radius in kilometers
     */
    fun getRadius(): Double {
        return sharedPreferences.getFloat(KEY_RADIUS, 5.0f).toDouble()
    }

    /**
     * Save FCM token for push notifications
     */
    fun saveFcmToken(token: String) {
        sharedPreferences.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    /**
     * Get FCM token
     */
    fun getFcmToken(): String? {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null)
    }

    /**
     * Save current room ID
     */
    fun saveCurrentRoomId(roomId: String?) {
        if (roomId != null) {
            sharedPreferences.edit().putString(KEY_CURRENT_ROOM_ID, roomId).apply()
        } else {
            sharedPreferences.edit().remove(KEY_CURRENT_ROOM_ID).apply()
        }
    }

    /**
     * Get current room ID
     */
    fun getCurrentRoomId(): String? {
        return sharedPreferences.getString(KEY_CURRENT_ROOM_ID, null)
    }

    /**
     * Save current group ID
     */
    fun saveCurrentGroupId(groupId: String?) {
        if (groupId != null) {
            sharedPreferences.edit().putString(KEY_CURRENT_GROUP_ID, groupId).apply()
        } else {
            sharedPreferences.edit().remove(KEY_CURRENT_GROUP_ID).apply()
        }
    }

    /**
     * Get current group ID
     */
    fun getCurrentGroupId(): String? {
        return sharedPreferences.getString(KEY_CURRENT_GROUP_ID, null)
    }

    /**
     * Clear all preferences (except FCM token)
     */
    fun clearAll() {
        val fcmToken = getFcmToken()
        sharedPreferences.edit().clear().apply()
        fcmToken?.let { saveFcmToken(it) }
    }

    /**
     * Clear room and group IDs (when leaving/completing)
     */
    fun clearRoomAndGroup() {
        sharedPreferences.edit()
            .remove(KEY_CURRENT_ROOM_ID)
            .remove(KEY_CURRENT_GROUP_ID)
            .apply()
    }
}