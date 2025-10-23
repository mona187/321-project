package com.example.cpen_321.data.repository

import com.example.cpen_321.data.network.api.UserApi
import com.example.cpen_321.data.network.api.MatchingApi
import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.network.api.JoinMatchingRequest
import com.example.cpen_321.data.network.api.JoinMatchingResponse
import retrofit2.Response
import javax.inject.Inject

class MatchRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val matchingApi: MatchingApi
) : MatchRepository {

    override suspend fun getUserProfilesForRoom(memberIds: List<Int>): Response<List<UserProfile>> {
        return userApi.getUserProfiles(memberIds.joinToString(",")) // Convert list to comma-separated string
    }

    override suspend fun joinMatching(request: JoinMatchingRequest): Response<JoinMatchingResponse> {
        val response = matchingApi.joinMatching(request)
        return if (response.isSuccessful) {
            Response.success(response.body()?.data ?: throw Exception("No data in response"))
        } else {
            Response.error(response.code(), response.errorBody() ?: throw Exception("Failed to join matching"))
        }
    }
}
