package com.example.cpen_321.data.network.dto

import com.example.cpen_321.data.model.Restaurant
import com.google.gson.annotations.SerializedName

/**
 * Vote for restaurant request
 * POST /api/group/vote/:groupId
 */
data class VoteRestaurantRequest(
    @SerializedName("restaurantID")
    val restaurantID: String,

    @SerializedName("restaurant")
    val restaurant: Restaurant? = null
)

/**
 * Vote response
 */
data class VoteRestaurantResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("Current_votes")
    val currentVotes: Map<String, Int>
)

/**
 * Leave group request
 * POST /api/group/leave/:groupId
 */
data class LeaveGroupRequest(
    @SerializedName("status")
    val status: String? = null
)

/**
 * Leave group response
 */
data class LeaveGroupResponse(
    @SerializedName("groupId")
    val groupId: String
)