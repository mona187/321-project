package com.example.cpen_321.data.repository

import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.network.api.JoinMatchingRequest
import com.example.cpen_321.data.network.api.JoinMatchingResponse
import retrofit2.Response

interface MatchRepository {
    suspend fun getUserProfilesForRoom(memberIds: List<Int>): Response<List<UserProfile>>
    suspend fun joinMatching(request: JoinMatchingRequest): Response<JoinMatchingResponse>
}