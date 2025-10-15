// data/repository/IMatchRepository.kt
package com.example.cpen_321.data.repository

import com.example.cpen_321.data.model.UserProfile
import retrofit2.Response

interface IMatchRepository {
    suspend fun getUserProfilesForRoom(memberIds: List<String>): Response<List<UserProfile>>
}
