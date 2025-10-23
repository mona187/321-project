package com.example.cpen_321.data.repository

import com.example.cpen_321.data.model.Restaurant
import com.example.cpen_321.data.model.RestaurantRecommendation
import com.example.cpen_321.data.network.api.RestaurantApi
import com.example.cpen_321.data.network.api.UserPreference
import retrofit2.Response
import javax.inject.Inject

class RestaurantRepositoryImpl @Inject constructor(
    private val restaurantApi: RestaurantApi
) : RestaurantRepository {

    override suspend fun searchRestaurants(
        latitude: Double,
        longitude: Double,
        radius: Int,
        cuisineTypes: String?,
        priceLevel: Int?
    ): Response<List<Restaurant>> {
        val response = restaurantApi.searchRestaurants(latitude, longitude, radius, cuisineTypes, priceLevel)
        return if (response.isSuccessful) {
            Response.success(response.body()?.data ?: emptyList())
        } else {
            Response.error(response.code(), response.errorBody() ?: throw Exception("Failed to search restaurants"))
        }
    }

    override suspend fun getRestaurantDetails(restaurantId: String): Response<Restaurant> {
        val response = restaurantApi.getRestaurantDetails(restaurantId)
        return if (response.isSuccessful) {
            Response.success(response.body()?.data ?: throw Exception("Restaurant not found"))
        } else {
            Response.error(response.code(), response.errorBody() ?: throw Exception("Failed to get restaurant details"))
        }
    }

    override suspend fun getGroupRecommendations(
        groupId: String,
        userPreferences: List<UserPreference>
    ): Response<List<RestaurantRecommendation>> {
        val response = restaurantApi.getGroupRecommendations(groupId, userPreferences)
        return if (response.isSuccessful) {
            Response.success(response.body()?.data ?: emptyList())
        } else {
            Response.error(response.code(), response.errorBody() ?: throw Exception("Failed to get group recommendations"))
        }
    }
}
