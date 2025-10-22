package com.example.cpen_321.data.model

import com.google.gson.annotations.SerializedName

/**
 * Group model for voting and restaurant selection
 */
data class Group(
    @SerializedName("groupId")
    val groupId: String? = null,

    @SerializedName("roomId")
    val roomId: String,

    @SerializedName("completionTime")
    val completionTime: Long,

    @SerializedName("numMembers")
    val numMembers: Int,

    @SerializedName("maxMembers")
    val maxMembers: Int? = null,

    @SerializedName("users")
    val users: List<String> = emptyList(),

    @SerializedName("members")
    val members: List<String>? = null, // Alternative field name from backend

    @SerializedName("restaurantSelected")
    val restaurantSelected: Boolean = false,

    @SerializedName("restaurant")
    val restaurant: Restaurant? = null,

    @SerializedName("votes")
    val votes: Map<String, String>? = null, // userId -> restaurantId

    @SerializedName("restaurantVotes")
    val restaurantVotes: Map<String, Int>? = null, // restaurantId -> vote count

    @SerializedName("status")
    val status: GroupStatus? = null
) {
    /**
     * Get all members (handles both 'users' and 'members' field names)
     */
    fun getAllMembers(): List<String> {
        return members ?: users
    }
}

/**
 * Group status enum
 */
enum class GroupStatus {
    @SerializedName("voting")
    VOTING,

    @SerializedName("completed")
    COMPLETED,

    @SerializedName("expired")
    EXPIRED,

    @SerializedName("matched")
    MATCHED,

    @SerializedName("disbanded")
    DISBANDED
}

/**
 * Vote response from server
 */
data class VoteResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("Current_votes")
    val currentVotes: Map<String, Int>
)

/**
 * Group member details (for displaying in group screen)
 */
data class GroupMember(
    val userId: String,
    val name: String,
    val credibilityScore: Double,
    val phoneNumber: String?,
    val profilePicture: String?,
    val hasVoted: Boolean = false
)