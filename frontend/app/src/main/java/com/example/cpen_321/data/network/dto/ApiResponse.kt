package com.example.cpen_321.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Standard API response format matching backend
 * Response Format:
 * {
 *   "Status": 200,
 *   "Message": { "error": "...", "text": "..." },
 *   "Body": { ... }
 * }
 */
data class ApiResponse<T>(
    @SerializedName("Status")
    val status: Int,

    @SerializedName("Message")
    val message: Message,

    @SerializedName("Body")
    val body: T?
)

/**
 * Message object in API response
 */
data class Message(
    @SerializedName("error")
    val error: String? = null,

    @SerializedName("text")
    val text: String? = null
) {
    fun isError(): Boolean = error != null

    fun getDisplayMessage(): String {
        return error ?: text ?: "Unknown response"
    }
}

/**
 * Simple response wrapper for responses without Body
 */
data class SimpleApiResponse(
    @SerializedName("Status")
    val status: Int,

    @SerializedName("Message")
    val message: Message
)

/**
 * Result wrapper for handling API responses in repositories
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}