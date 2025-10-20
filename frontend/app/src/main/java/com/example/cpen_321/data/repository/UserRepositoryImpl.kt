package com.example.cpen_321.data.repository

import android.util.Log
import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.network.api.UserApi
import javax.inject.Inject
import javax.inject.Singleton

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
}