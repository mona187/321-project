package com.example.cpen_321.data.repository

import com.example.cpen_321.data.model.UserProfile

interface UserRepository {
    suspend fun getCurrentUserProfile(): UserProfile?
    suspend fun getUserProfile(userId: Int): UserProfile?
}
