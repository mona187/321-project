package com.example.cpen_321.data.repository

import com.example.cpen_321.data.model.Group
import com.example.cpen_321.data.model.Restaurant
import com.example.cpen_321.data.network.dto.ApiResult

/**
 * Repository interface for group operations
 */
interface GroupRepository {

    /**
     * Get current user's group status
     */
    suspend fun getGroupStatus(): ApiResult<Group>

    /**
     * Vote for a restaurant
     */
    suspend fun voteForRestaurant(
        groupId: String,
        restaurantId: String,
        restaurant: Restaurant? = null
    ): ApiResult<Map<String, Int>> // Returns current votes

    /**
     * Leave a group
     */
    suspend fun leaveGroup(groupId: String): ApiResult<String>

    /**
     * Save current group ID locally
     */
    fun saveCurrentGroupId(groupId: String?)

    /**
     * Get current group ID from local storage
     */
    fun getCurrentGroupId(): String?
}