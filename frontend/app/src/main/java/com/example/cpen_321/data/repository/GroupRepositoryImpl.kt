package com.example.cpen_321.data.repository

import com.example.cpen_321.data.local.PreferencesManager
import com.example.cpen_321.data.model.Group
import com.example.cpen_321.data.model.Restaurant
import com.example.cpen_321.data.network.RetrofitClient
import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.network.dto.LeaveGroupRequest
import com.example.cpen_321.data.network.dto.VoteRestaurantRequest
import com.example.cpen_321.data.network.dto.map
import com.example.cpen_321.data.network.safeApiCall
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Implementation of GroupRepository
 */
class GroupRepositoryImpl(
    private val preferencesManager: PreferencesManager
) : GroupRepository {

    private val groupAPI = RetrofitClient.groupAPI

    override suspend fun getGroupStatus(): ApiResult<Group> {
        val response =safeApiCall(
            apiCall = {groupAPI.getGroupStatus()},
            customErrorCode = "Failed to get group status"
        ).also {
            apiResult ->
            if (apiResult is ApiResult.Success) {
                val groupStatus = apiResult.data

                groupStatus.groupId?.let { newGroupId ->
                    if (getCurrentGroupId() == null) {
                        saveCurrentGroupId(newGroupId)
                    }
                }
            }
        }

        return response;
    }

    override suspend fun voteForRestaurant(
        groupId: String,
        restaurantId: String,
        restaurant: Restaurant?
    ): ApiResult<Map<String, Int>> {

        val request = VoteRestaurantRequest(
            restaurantID = restaurantId,
            restaurant = restaurant
        )

        val response = safeApiCall(
            apiCall = {groupAPI.voteForRestaurant(groupId,request)},
            customErrorCode = "Failed to vote"
        ).map { apiResponse -> apiResponse.currentVotes }

        return response;
    }

    override suspend fun leaveGroup(groupId: String): ApiResult<String> {

        val request = LeaveGroupRequest()
        val response = safeApiCall(
            apiCall = { groupAPI.leaveGroup(
                groupId = groupId,
                request = request
            )},
                customErrorCode = "Failed to leave group"
        ).map{"Left group successfully"}

        // Clear local group ID
        saveCurrentGroupId(null)
        return response;
    }

    override fun saveCurrentGroupId(groupId: String?) {
        preferencesManager.saveCurrentGroupId(groupId)
    }

    override fun getCurrentGroupId(): String? {
        return preferencesManager.getCurrentGroupId()
    }
}