package com.example.cpen_321.data.repository

import com.example.cpen_321.data.network.api.UserApi
import com.example.cpen_321.data.model.UserProfile
import retrofit2.Response
import javax.inject.Inject

class MatchRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : MatchRepository {

    override suspend fun getUserProfilesForRoom(memberIds: List<Int>): Response<List<UserProfile>> {
        return userApi.getUserProfiles(memberIds.joinToString(",")) // Convert list to comma-separated string
    }
}
