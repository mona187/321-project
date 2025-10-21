package com.example.cpen_321.data.repository

import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.model.User
interface UserRepository {
    suspend fun getCurrentUserProfile(): UserProfile?
    suspend fun getUserProfile(userId: Int): UserProfile?

    suspend fun getUserSettings(): User?

    suspend fun updateUserSettings(user: User): User?

    suspend fun createUserSettings(user: User): User?

}
