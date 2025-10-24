package com.example.cpen_321.data.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

/**
 * Room model for waiting rooms during matching
 */
data class Room(
    @SerializedName("roomId")
    val roomId: String,

    @SerializedName("completionTime")
    val completionTime: String,  // Changed from Long to String to handle ISO 8601 format

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
) {
    /**
     * Parse the ISO 8601 completionTime string to a timestamp in milliseconds
     */
    fun getCompletionTimeMillis(): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(completionTime)?.time ?: 0L
        } catch (e: Exception) {
            // If parsing fails, try to parse as Long (backwards compatibility)
            try {
                completionTime.toLong()
            } catch (e: NumberFormatException) {
                0L
            }
        }
    }

    /**
     * Get the completion time as a Date object
     */
    fun getCompletionDate(): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(completionTime)
        } catch (e: Exception) {
            null
        }
    }
}

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
    val completionTime: String,  // Changed from Long to String

    @SerializedName("members")
    val members: List<String>,

    @SerializedName("groupReady")
    val groupReady: Boolean,

    @SerializedName("status")
    val status: String
) {
    /**
     * Parse the ISO 8601 completionTime string to a timestamp in milliseconds
     */
    fun getCompletionTimeMillis(): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(completionTime)?.time ?: 0L
        } catch (e: Exception) {
            // If parsing fails, try to parse as Long (backwards compatibility)
            try {
                completionTime.toLong()
            } catch (e: NumberFormatException) {
                0L
            }
        }
    }
}

/**
 * Room users response from API
 */
data class RoomUsersResponse(
    @SerializedName("roomID")
    val roomID: String,

    @SerializedName("Users")
    val users: List<String>
)