package com.example.cpen_321.data.network.dto

import com.example.cpen_321.data.model.Room
import com.google.gson.annotations.SerializedName

/**
 * Join matching request
 * POST /api/matching/join
 */
data class JoinMatchingRequest(
    @SerializedName("cuisine")
    val cuisine: List<String>? = null,

    @SerializedName("budget")
    val budget: Double? = null,

    @SerializedName("radiusKm")
    val radiusKm: Double? = null
)

/**
 * Join matching response
 */
data class JoinMatchingResponse(
    @SerializedName("roomId")
    val roomId: String,

    @SerializedName("room")
    val room: Room
)

/**
 * Leave room request
 * PUT /api/matching/leave/:roomId
 */
data class LeaveRoomRequest(
    @SerializedName("status")
    val status: String? = null
)

/**
 * Leave room response
 */
data class LeaveRoomResponse(
    @SerializedName("roomId")
    val roomId: String
)