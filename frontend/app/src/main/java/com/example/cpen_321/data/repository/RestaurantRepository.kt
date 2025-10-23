package com.example.cpen_321.data.repository

import com.example.cpen_321.data.model.Restaurant
import com.example.cpen_321.data.model.RestaurantRecommendation
import com.example.cpen_321.data.network.api.UserPreference
import retrofit2.Response

interface RestaurantRepository {
    suspend fun searchRestaurants(
        latitude: Double,
        longitude: Double,
        radius: Int,
        cuisineTypes: String?,
        priceLevel: Int?
    ): Response<List<Restaurant>>

    suspend fun getRestaurantDetails(restaurantId: String): Response<Restaurant>

    suspend fun getGroupRecommendations(
        groupId: String,
        userPreferences: List<UserPreference>
    ): Response<List<RestaurantRecommendation>>
}
