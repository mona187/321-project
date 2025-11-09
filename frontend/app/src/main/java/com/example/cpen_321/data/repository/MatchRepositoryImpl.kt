package com.example.cpen_321.data.repository

import android.graphics.Paint
import android.util.Log
import com.example.cpen_321.data.local.PreferencesManager
import com.example.cpen_321.data.model.Room
import com.example.cpen_321.data.model.RoomStatusResponse
import com.example.cpen_321.data.model.UserSettings
import com.example.cpen_321.data.network.RetrofitClient
import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.network.dto.JoinMatchingRequest
import com.example.cpen_321.data.network.dto.LeaveRoomRequest
import com.example.cpen_321.data.network.dto.map
import com.example.cpen_321.data.network.safeApiCall
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
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

    suspend fun JoinMatchingUserInRoom(
        userSettings: UserSettings
    ){

        Log.d(TAG, "User has stale roomID: ${userSettings.roomID}, cleaning up...")

        // Try to leave the room (might fail if room doesn't exist anymore)
        if (userSettings.roomID == null) {
            throw IllegalStateException("JoinMatchingUserInRoom cannot be called with null room (precondition invalidated)")
        }
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

    override suspend fun joinMatching(
        cuisine: List<String>?,
        budget: Double?,
        radiusKm: Double?
    ): ApiResult<Pair<String, Room>> {


        // Check if user is already in a room and clean up if needed
        val userSettingsResult = userRepository.getUserSettings()

        if (userSettingsResult is ApiResult.Success) {
            val userSettings = userSettingsResult.data

            // If user is in a group, they can't join matching
            if (!userSettings.groupID.isNullOrEmpty()) {
                return ApiResult.Error("Please leave your current group before joining matching")
            }

            // If user is in a room, try to leave it first
            if (!userSettings.roomID.isNullOrEmpty()) {
                JoinMatchingUserInRoom(userSettings)
            }
        }

        val request = JoinMatchingRequest(
            cuisine = cuisine,
            budget = budget,
            radiusKm = radiusKm
        )

        val apiResult = safeApiCall(
            apiCall = {matchAPI.joinMatching(request)},
            customErrorCode = "Failed to join matching"
        )
            .also { apiResult ->
                if (apiResult is ApiResult.Success) {
                    // Save room ID locally
                    saveCurrentRoomId(apiResult.data.roomId)
                    Log.d(TAG, "Successfully joined matching, roomId: ${apiResult.data.roomId}")
                }
            }
            .map {joinResponse -> Pair(joinResponse.roomId, joinResponse.room) } /* exports the success to be that */

        if (apiResult is ApiResult.Success){
            // Save preferences locally
            cuisine?.let { preferencesManager.saveCuisines(it.toSet()) }
            budget?.let { preferencesManager.saveBudget(it) }
            radiusKm?.let { preferencesManager.saveRadius(it) }
        }

        return apiResult
    }

    override suspend fun leaveRoom(roomId: String): ApiResult<String> {

        val response = safeApiCall(
            apiCall = { matchAPI.leaveRoom(roomId, LeaveRoomRequest()) },
            customErrorCode = "Failed to leave room"
        ).also { apiResult ->
                // Use 'when' for an exhaustive check of the ApiResult
                when (apiResult) {
                    is ApiResult.Success -> {
                        // This is the primary success path.
                        saveCurrentRoomId(null)
                        Log.d(TAG, "Left room successfully: $roomId")
                    }
                    is ApiResult.Error -> {
                        // This is the error path, where we check for the special case.
                        if (apiResult.message.contains("Room not found", ignoreCase = true) ||
                            apiResult.message.contains("not found", ignoreCase = true)) {
                            // Special case: Treat "not found" as a trigger to clear local state.
                            saveCurrentRoomId(null)
                            Log.d(TAG, "Room $roomId not found - clearing local state anyway.")
                        } else {
                            // This is a genuine, unexpected error.
                            Log.e(TAG, "Failed to leave room: ${apiResult.message} (Code: ${apiResult.code})")
                        }
                    }
                    is ApiResult.Loading -> { /* No side-effects needed for loading state */ }
                }
            }
            .map {
                /* success string */
                "Left room successfully"
            }

        return response;
    }

    override suspend fun getRoomStatus(roomId: String): ApiResult<RoomStatusResponse> {
        val response = safeApiCall(apiCall = {matchAPI.getRoomStatus(roomId)}, customErrorCode = "Failed to get room status")
        return response;
    }

    override suspend fun getRoomUsers(roomId: String): ApiResult<List<String>> {

        val response = safeApiCall(apiCall =  {
            matchAPI.getRoomUsers(roomId)},
            customErrorCode = "Failed to get room users"
        ).map { apiResponse -> apiResponse.users  }

        return response;

    }

    override fun saveCurrentRoomId(roomId: String?) {
        preferencesManager.saveCurrentRoomId(roomId)
    }

    override fun getCurrentRoomId(): String? {
        return preferencesManager.getCurrentRoomId()
    }
}