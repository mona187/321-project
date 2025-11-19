package com.example.cpen_321.data.model

import com.google.gson.annotations.SerializedName
import java.text.ParseException
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
            // Try multiple date formats
            val formats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            )
            
            formats.forEach { format ->
                format.timeZone = TimeZone.getTimeZone("UTC")
                try {
                    val parsed = format.parse(completionTime)
                    if (parsed != null) {
                        return parsed.time
                    }
                } catch (e: ParseException) {
                    // Try next format
                }
            }
            
            // If all formats fail, try to parse as Long (backwards compatibility)
            completionTime.toLong()
        } catch (e: Exception) {
            android.util.Log.e("Room", "Failed to parse completionTime: $completionTime", e)
            0L
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
        } catch (e: ParseException) {
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
            // Try multiple date formats
            val formats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            )
            
            formats.forEach { format ->
                format.timeZone = TimeZone.getTimeZone("UTC")
                try {
                    val parsed = format.parse(completionTime)
                    if (parsed != null) {
                        return parsed.time
                    }
                } catch (e: ParseException) {
                    // Try next format
                }
            }
            
            // If all formats fail, try to parse as Long (backwards compatibility)
            completionTime.toLong()
        } catch (e: Exception) {
            android.util.Log.e("RoomStatusResponse", "Failed to parse completionTime: $completionTime", e)
            0L
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