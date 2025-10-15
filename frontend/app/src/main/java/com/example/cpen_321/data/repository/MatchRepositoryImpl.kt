package com.example.cpen_321.data.repository

import com.example.cpen_321.data.network.api.MatchApi
import com.example.cpen_321.data.model.UserProfile
import retrofit2.Response
import javax.inject.Inject

class MatchRepositoryImpl @Inject constructor(
    private val matchApi: MatchApi
) : MatchRepository {

    override suspend fun getUserProfilesForRoom(memberIds: List<String>): Response<List<UserProfile>> {
        val idsParam = memberIds.joinToString(",")
        return matchApi.getUserProfiles(idsParam)
    }
}
