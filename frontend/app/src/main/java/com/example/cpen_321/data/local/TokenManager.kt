package com.cpen321.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "TokenManager"
    }

    private val tokenKey = stringPreferencesKey("auth_token")

    suspend fun saveToken(token: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[tokenKey] = token
            }
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while saving token", e)
            throw e
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to save token", e)
            throw e
        }
    }

    fun getToken(): Flow<String?> = context.dataStore.data.map { it[tokenKey] }

    suspend fun getTokenSync(): String? =
        try {
            context.dataStore.data.first()[tokenKey]
        } catch (e: Exception) {
            Log.e(TAG, "Error while getting token synchronously", e)
            null
        }

    suspend fun clearToken() {
        try {
            context.dataStore.edit { it.remove(tokenKey) }
        } catch (e: Exception) {
            Log.e(TAG, "Error while clearing token", e)
            throw e
        }
    }
}
