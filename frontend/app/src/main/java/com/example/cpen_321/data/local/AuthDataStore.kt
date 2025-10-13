//package com.example.cpen_321.data.local
//
//import android.content.Context
//import androidx.datastore.core.DataStore
//import androidx.datastore.preferences.core.Preferences
//import androidx.datastore.preferences.core.booleanPreferencesKey
//import androidx.datastore.preferences.core.edit
//import androidx.datastore.preferences.core.stringPreferencesKey
//import androidx.datastore.preferences.preferencesDataStore
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.map
//
//private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")
//
//class AuthDataStore(private val context: Context) {
//
//    companion object {
//        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
//        private val USER_ID_KEY = stringPreferencesKey("user_id")
//        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
//        private val USER_USERNAME_KEY = stringPreferencesKey("user_username")
//        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
//    }
//
//    suspend fun saveAuthData(
//        token: String,
//        userId: String,
//        email: String,
//        username: String
//    ) {
//        context.dataStore.edit { preferences ->
//            preferences[AUTH_TOKEN_KEY] = token
//            preferences[USER_ID_KEY] = userId
//            preferences[USER_EMAIL_KEY] = email
//            preferences[USER_USERNAME_KEY] = username
//            preferences[IS_LOGGED_IN_KEY] = true
//        }
//    }
//
//    suspend fun clearAuthData() {
//        context.dataStore.edit { preferences ->
//            preferences.remove(AUTH_TOKEN_KEY)
//            preferences.remove(USER_ID_KEY)
//            preferences.remove(USER_EMAIL_KEY)
//            preferences.remove(USER_USERNAME_KEY)
//            preferences[IS_LOGGED_IN_KEY] = false
//        }
//    }
//
//    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
//        preferences[AUTH_TOKEN_KEY]
//    }
//
//    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
//        preferences[IS_LOGGED_IN_KEY] ?: false
//    }
//
//    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
//        preferences[USER_ID_KEY]
//    }
//
//    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
//        preferences[USER_EMAIL_KEY]
//    }
//
//    val username: Flow<String?> = context.dataStore.data.map { preferences ->
//        preferences[USER_USERNAME_KEY]
//    }
//}
