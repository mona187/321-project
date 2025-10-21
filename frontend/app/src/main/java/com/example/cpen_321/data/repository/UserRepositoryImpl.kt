package com.example.cpen_321.data.repository

import android.util.Log
import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.network.api.UserApi
import javax.inject.Inject
import javax.inject.Singleton
import com.example.cpen_321.data.model.User
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val authRepository: AuthRepositoryImpl
): UserRepository {

    companion object {
        private const val TAG = "UserRepository"
    }

    override suspend fun getCurrentUserProfile(): UserProfile? { // For viewing current authenticated user profile
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            Log.e(TAG, "No cached user found. User might not be authenticated.")
            return null
        }

        val response = userApi.getUserProfiles(listOf(currentUser.userId)) // pass list even for single user
        return if (response.isSuccessful && !response.body().isNullOrEmpty()) {
            response.body()!!.first()
        } else {
            Log.e(TAG, "Failed to fetch current user profile: ${response.errorBody()?.string()}")
            null
        }
    }

    override suspend fun getUserProfile(userId: Int): UserProfile? { // For viewing other user profiles
        val response = userApi.getUserProfiles(listOf(userId))
        return if (response.isSuccessful && !response.body().isNullOrEmpty()) {
            response.body()!!.first()
        } else {
            Log.e(TAG, "Failed to fetch profile for userId=$userId")
            null
        }
    }

    override suspend fun createUserSettings(user: User): User {
        val response = userApi.createUserSettings(user)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("Failed to create user settings: ${response.errorBody()?.string()}")
        }
    }

    // 🟦 Fetch current user settings
    override suspend fun getUserSettings(): User {
        return try {
            val response = userApi.getUserSettings()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch user settings"
                Log.e(TAG, "Error fetching user: $errorMsg")
                throw Exception(errorMsg)
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Network timeout during getUserSettings", e)
            throw e
        } catch (e: UnknownHostException) {
            Log.e(TAG, "No network during getUserSettings", e)
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "IO exception during getUserSettings", e)
            throw e
        }
    }

    // 🟨 Update current user settings
    override suspend fun updateUserSettings(user: User): User {
        return try {
            val response = userApi.updateUserSettings(user)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to update user settings"
                Log.e(TAG, "Error updating user: $errorMsg")
                throw Exception(errorMsg)
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Network timeout during updateUserSettings", e)
            throw e
        } catch (e: UnknownHostException) {
            Log.e(TAG, "No network during updateUserSettings", e)
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "IO exception during updateUserSettings", e)
            throw e
        }
    }
}

