package com.example.cpen_321.data.repository

import com.example.cpen_321.data.model.Restaurant
import com.example.cpen_321.data.network.dto.ApiResult

/**
 * Repository interface for restaurant operations
 */
interface RestaurantRepository {

    /**
     * Search for restaurants near a location
     */
    suspend fun searchRestaurants(
        latitude: Double,
        longitude: Double,
        radius: Int? = null,
        cuisineTypes: List<String>? = null,
        priceLevel: Int? = null
    ): ApiResult<List<Restaurant>>

    /**
     * Get restaurant details by ID
     */
    suspend fun getRestaurantDetails(restaurantId: String): ApiResult<Restaurant>

    /**
     * Get restaurant recommendations for a group
     */
    suspend fun getGroupRecommendations(
        groupId: String,
        userPreferences: List<UserPreferenceData>
    ): ApiResult<List<Restaurant>>
}

/**
 * User preference data for group recommendations
 */
data class UserPreferenceData(
    val cuisineTypes: List<String>,
    val budget: Double,
    val latitude: Double,
    val longitude: Double,
    val radiusKm: Double
)