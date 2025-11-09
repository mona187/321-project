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
            } catch (e: IOException) {
                ApiResult.Error("Network error: ${e.localizedMessage}")
            } catch (e: HttpException) {
                ApiResult.Error("HTTP error ${e.code()}: ${e.message()}", code = e.code())
            } catch (e: JsonSyntaxException) {
                ApiResult.Error("Parsing error: ${e.localizedMessage}")
            } catch (e: Exception) {
                ApiResult.Error("Unexpected error: ${e.localizedMessage}")
            }
        }
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