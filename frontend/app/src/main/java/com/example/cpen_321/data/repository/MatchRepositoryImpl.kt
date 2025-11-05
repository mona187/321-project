package com.example.cpen_321.data.repository

import android.util.Log
import com.example.cpen_321.data.local.PreferencesManager
import com.example.cpen_321.data.model.Room
import com.example.cpen_321.data.model.RoomStatusResponse
import com.example.cpen_321.data.network.RetrofitClient
import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.network.dto.JoinMatchingRequest
import com.example.cpen_321.data.network.dto.LeaveRoomRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Implementation of MatchRepository
 */
class MatchRepositoryImpl(
    private val preferencesManager: PreferencesManager,
    private val userRepository: UserRepository
) : MatchRepository {

    private val matchAPI = RetrofitClient.matchAPI
    private val TAG = "MatchRepository"

    override suspend fun joinMatching(
        cuisine: List<String>?,
        budget: Double?,
        radiusKm: Double?
    ): ApiResult<Pair<String, Room>> {
        return withContext(Dispatchers.IO) {
            try {
                // Check if user is already in a room and clean up if needed
                val userSettingsResult = userRepository.getUserSettings()

                if (userSettingsResult is ApiResult.Success) {
                    val userSettings = userSettingsResult.data

                    // If user is in a group, they can't join matching
                    if (!userSettings.groupID.isNullOrEmpty()) {
                        return@withContext ApiResult.Error("Please leave your current group before joining matching")
                    }

                    // If user is in a room, try to leave it first
                    if (!userSettings.roomID.isNullOrEmpty()) {
                        Log.d(TAG, "User has stale roomID: ${userSettings.roomID}, cleaning up...")

                        // Try to leave the room (might fail if room doesn't exist anymore)
                        val leaveResult = leaveRoom(userSettings.roomID)

                        when (leaveResult) {
                            is ApiResult.Success -> {
                                Log.d(TAG, "Successfully left previous room")
                                delay(300) // Give backend time to process
                            }
                            is ApiResult.Error -> {
                                // Room might not exist anymore - that's okay!
                                if (leaveResult.message?.contains("Room not found") == true ||
                                    leaveResult.message?.contains("not found") == true) {
                                    Log.d(TAG, "Previous room no longer exists - this is fine, continuing...")
                                    // Clear the stale roomID locally
                                    saveCurrentRoomId(null)
                                } else {
                                    Log.w(TAG, "Error leaving room: ${leaveResult.message}, continuing anyway...")
                                }
                            }
                            is ApiResult.Loading -> {
                                // Should not happen, but ignore
                            }
                        }
                    }
                }

                // Now try to join matching
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

                        Log.d(TAG, "Successfully joined matching, roomId: ${joinResponse.roomId}")
                        ApiResult.Success(Pair(joinResponse.roomId, joinResponse.room))
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to join matching: $errorBody")
                    ApiResult.Error(
                        message = errorBody ?: "Failed to join matching",
                        code = response.code()
                    )
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error joining matching", e)
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

                    Log.d(TAG, "Left room successfully: $roomId")
                    ApiResult.Success("Left room successfully")
                } else {
                    val errorBody = response.errorBody()?.string()

                    // If room not found, that's actually okay - clear local state
                    if (errorBody?.contains("Room not found") == true ||
                        errorBody?.contains("not found") == true) {
                        saveCurrentRoomId(null)
                        Log.d(TAG, "Room $roomId not found - clearing local state")
                    } else {
                        Log.e(TAG, "Failed to leave room: $errorBody")
                    }

                    ApiResult.Error(
                        message = errorBody ?: "Failed to leave room",
                        code = response.code()
                    )
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error leaving room", e)
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
            } catch (e: IOException) {
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
            } catch (e: IOException) {
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