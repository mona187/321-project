package com.example.cpen_321.data.repository

import com.example.cpen_321.data.local.PreferencesManager
import com.example.cpen_321.data.model.Room
import com.example.cpen_321.data.model.RoomStatusResponse
import com.example.cpen_321.data.network.RetrofitClient
import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.network.dto.JoinMatchingRequest
import com.example.cpen_321.data.network.dto.LeaveRoomRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of MatchRepository
 */
class MatchRepositoryImpl(
    private val preferencesManager: PreferencesManager
) : MatchRepository {

    private val matchAPI = RetrofitClient.matchAPI

    override suspend fun joinMatching(
        cuisine: List<String>?,
        budget: Double?,
        radiusKm: Double?
    ): ApiResult<Pair<String, Room>> {
        return withContext(Dispatchers.IO) {
            try {
                val request = JoinMatchingRequest(
                    cuisine = cuisine,
                    budget = budget,
                    radiusKm = radiusKm
                )

                val response = matchAPI.joinMatching(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.body != null) {
                        val joinResponse = apiResponse.body

                        // Save room ID locally
                        saveCurrentRoomId(joinResponse.roomId)

                        // Save preferences locally
                        cuisine?.let { preferencesManager.saveCuisines(it.toSet()) }
                        budget?.let { preferencesManager.saveBudget(it) }
                        radiusKm?.let { preferencesManager.saveRadius(it) }

                        ApiResult.Success(Pair(joinResponse.roomId, joinResponse.room))
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    ApiResult.Error(
                        message = errorBody ?: "Failed to join matching",
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

    override suspend fun leaveRoom(roomId: String): ApiResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LeaveRoomRequest()
                val response = matchAPI.leaveRoom(roomId, request)

                if (response.isSuccessful) {
                    // Clear local room ID
                    saveCurrentRoomId(null)

                    ApiResult.Success("Left room successfully")
                } else {
                    val errorBody = response.errorBody()?.string()
                    ApiResult.Error(
                        message = errorBody ?: "Failed to leave room",
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

    override suspend fun getRoomStatus(roomId: String): ApiResult<RoomStatusResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = matchAPI.getRoomStatus(roomId)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.body != null) {
                        ApiResult.Success(apiResponse.body)
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    ApiResult.Error(
                        message = response.errorBody()?.string() ?: "Failed to get room status",
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

    override suspend fun getRoomUsers(roomId: String): ApiResult<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = matchAPI.getRoomUsers(roomId)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.body != null) {
                        ApiResult.Success(apiResponse.body.users)
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    ApiResult.Error(
                        message = response.errorBody()?.string() ?: "Failed to get room users",
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

    override fun saveCurrentRoomId(roomId: String?) {
        preferencesManager.saveCurrentRoomId(roomId)
    }

    override fun getCurrentRoomId(): String? {
        return preferencesManager.getCurrentRoomId()
    }
}