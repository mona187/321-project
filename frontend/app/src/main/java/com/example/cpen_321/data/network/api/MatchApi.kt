package com.example.cpen_321.data.network.api

import com.example.cpen_321.data.model.RoomStatusResponse
import com.example.cpen_321.data.model.RoomUsersResponse
import com.example.cpen_321.data.network.dto.ApiResponse
import com.example.cpen_321.data.network.dto.JoinMatchingRequest
import com.example.cpen_321.data.network.dto.JoinMatchingResponse
import com.example.cpen_321.data.network.dto.LeaveRoomRequest
import com.example.cpen_321.data.network.dto.LeaveRoomResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Matching/Room API endpoints
 */
interface MatchAPI {

    /**
     * POST /api/matching/join
     * Join the matching pool
     */
    @POST("api/matching/join")
    suspend fun joinMatching(
        @Body request: JoinMatchingRequest
    ): Response<ApiResponse<JoinMatchingResponse>>

    /**
     * POST /api/matching/join/:roomId
     * Join a specific room (not implemented in backend)
     */
    @POST("api/matching/join/{roomId}")
    suspend fun joinSpecificRoom(
        @Path("roomId") roomId: String
    ): Response<ApiResponse<JoinMatchingResponse>>

    /**
     * PUT /api/matching/leave/:roomId
     * Leave a waiting room
     */
    @PUT("api/matching/leave/{roomId}")
    suspend fun leaveRoom(
        @Path("roomId") roomId: String,
        @Body request: LeaveRoomRequest = LeaveRoomRequest()
    ): Response<ApiResponse<LeaveRoomResponse>>

    /**
     * GET /api/matching/status/:roomId
     * Get status of a waiting room
     */
    @GET("api/matching/status/{roomId}")
    suspend fun getRoomStatus(
        @Path("roomId") roomId: String
    ): Response<ApiResponse<RoomStatusResponse>>

    /**
     * GET /api/matching/users/:roomId
     * Get users in a room
     */
    @GET("api/matching/users/{roomId}")
    suspend fun getRoomUsers(
        @Path("roomId") roomId: String
    ): Response<ApiResponse<RoomUsersResponse>>
}