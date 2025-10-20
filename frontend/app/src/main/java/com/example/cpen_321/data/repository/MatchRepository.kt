package com.example.cpen_321.data.repository

import com.example.cpen_321.data.model.UserProfile
import retrofit2.Response

interface MatchRepository {
    suspend fun getUserProfilesForRoom(memberIds: List<Int>): Response<List<UserProfile>>
}