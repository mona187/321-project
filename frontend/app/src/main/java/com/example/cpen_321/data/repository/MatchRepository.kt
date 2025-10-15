package com.example.cpen_321.data.repository

import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.network.ApiService
import javax.inject.Inject
import retrofit2.Response

open class MatchRepository @Inject constructor(
    private val api: ApiService
) : IMatchRepository {
    override suspend fun getUserProfilesForRoom(memberIds: List<String>): Response<List<UserProfile>> {
        val idsParam = memberIds.joinToString(",")
        return api.getUserProfiles(idsParam)
    }
}