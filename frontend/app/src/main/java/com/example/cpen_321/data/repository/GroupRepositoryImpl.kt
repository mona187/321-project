package com.example.cpen_321.data.repository

import com.example.cpen_321.data.local.PreferencesManager
import com.example.cpen_321.data.model.Group
import com.example.cpen_321.data.model.Restaurant
import com.example.cpen_321.data.network.RetrofitClient
import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.network.dto.LeaveGroupRequest
import com.example.cpen_321.data.network.dto.VoteRestaurantRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of GroupRepository
 */
class GroupRepositoryImpl(
    private val preferencesManager: PreferencesManager
) : GroupRepository {

    private val groupAPI = RetrofitClient.groupAPI

    override suspend fun getGroupStatus(): ApiResult<Group> {
        return withContext(Dispatchers.IO) {
            try {
                val response = groupAPI.getGroupStatus()

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.body != null) {
                        // Save group ID locally if not already saved
                        apiResponse.body.groupId?.let { groupId ->
                            if (getCurrentGroupId() == null) {
                                saveCurrentGroupId(groupId)
                            }
                        }

                        ApiResult.Success(apiResponse.body)
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    ApiResult.Error(
                        message = response.errorBody()?.string() ?: "Failed to get group status",
                        code = response.code()
                    )
                }
            } catch (e: Exception) {
                ApiResult.Error(
                    message = e.localizedMessage ?: "Network error occurred",
                    code = null
                )
            }
        }
    }

    override suspend fun voteForRestaurant(
        groupId: String,
        restaurantId: String,
        restaurant: Restaurant?
    ): ApiResult<Map<String, Int>> {
        return withContext(Dispatchers.IO) {
            try {
                val request = VoteRestaurantRequest(
                    restaurantID = restaurantId,
                    restaurant = restaurant
                )

                val response = groupAPI.voteForRestaurant(groupId, request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.body != null) {
                        ApiResult.Success(apiResponse.body.currentVotes)
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    ApiResult.Error(
                        message = response.errorBody()?.string() ?: "Failed to vote",
                        code = response.code()
                    )
                }
            } catch (e: Exception) {
                ApiResult.Error(
                    message = e.localizedMessage ?: "Network error occurred",
                    code = null
                )
            }
        }
    }

    override suspend fun leaveGroup(groupId: String): ApiResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LeaveGroupRequest()
                val response = groupAPI.leaveGroup(groupId, request)

                if (response.isSuccessful) {
                    // Clear local group ID
                    saveCurrentGroupId(null)

                    ApiResult.Success("Left group successfully")
                } else {
                    ApiResult.Error(
                        message = response.errorBody()?.string() ?: "Failed to leave group",
                        code = response.code()
                    )
                }
            } catch (e: Exception) {
                ApiResult.Error(
                    message = e.localizedMessage ?: "Network error occurred",
                    code = null
                )
            }
        }
    }

    override fun saveCurrentGroupId(groupId: String?) {
        preferencesManager.saveCurrentGroupId(groupId)
    }

    override fun getCurrentGroupId(): String? {
        return preferencesManager.getCurrentGroupId()
    }
}