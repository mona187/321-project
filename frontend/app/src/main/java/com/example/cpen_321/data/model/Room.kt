package com.example.cpen_321.data.model

import com.google.gson.annotations.SerializedName

/**
 * Room model for waiting rooms during matching
 */
data class Room(
    @SerializedName("roomId")
    val roomId: String,

    @SerializedName("completionTime")
    val completionTime: Long,

    @SerializedName("maxMembers")
    val maxMembers: Int = 10,

    @SerializedName("members")
    val members: List<String> = emptyList(),

    @SerializedName("status")
    val status: RoomStatus = RoomStatus.WAITING,

    @SerializedName("cuisine")
    val cuisine: String? = null,

    @SerializedName("averageBudget")
    val averageBudget: Double? = null,

    @SerializedName("averageRadius")
    val averageRadius: Double? = null
)

/**
 * Room status enum
 */
enum class RoomStatus {
    @SerializedName("waiting")
    WAITING,

    @SerializedName("matched")
    MATCHED,

    @SerializedName("expired")
    EXPIRED
}

/**
 * Room status response from API
 */
data class RoomStatusResponse(
    @SerializedName("roomID")
    val roomID: String,

    @SerializedName("completionTime")
    val completionTime: Long,

    @SerializedName("members")
    val members: List<String>,

    @SerializedName("groupReady")
    val groupReady: Boolean,

    @SerializedName("status")
    val status: String
)

/**
 * Room users response from API
 */
data class RoomUsersResponse(
    @SerializedName("roomID")
    val roomID: String,

    @SerializedName("Users")
    val users: List<String>
)