package com.example.cpen_321.data.repository

import com.example.cpen_321.data.model.Restaurant
import com.example.cpen_321.data.network.RetrofitClient
import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.network.dto.GroupRecommendationsRequest
import com.example.cpen_321.data.network.dto.LocationDto
import com.example.cpen_321.data.network.dto.UserPreferenceDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Implementation of RestaurantRepository
 */
class RestaurantRepositoryImpl : RestaurantRepository {

    private val restaurantAPI = RetrofitClient.restaurantAPI

    override suspend fun searchRestaurants(
        latitude: Double,
        longitude: Double,
        radius: Int?,
        cuisineTypes: List<String>?,
        priceLevel: Int?
    ): ApiResult<List<Restaurant>> {
        return withContext(Dispatchers.IO) {
            try {
                // Convert cuisineTypes list to comma-separated string
                val cuisineTypesString = cuisineTypes?.joinToString(",")

                val response = restaurantAPI.searchRestaurants(
                    latitude = latitude,
                    longitude = longitude,
                    radius = radius,
                    cuisineTypes = cuisineTypesString,
                    priceLevel = priceLevel
                )

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.body != null) {
                        ApiResult.Success(apiResponse.body)
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    ApiResult.Error(
                        message = response.errorBody()?.string() ?: "Failed to search restaurants",
                        code = response.code()
                    )
                }
            } catch (e: IOException) {
                ApiResult.Error(
                    message = e.localizedMessage ?: "Network error occurred",
                    code = null
                )
            }
        }
    }

    override suspend fun getRestaurantDetails(restaurantId: String): ApiResult<Restaurant> {
        return withContext(Dispatchers.IO) {
            try {
                val response = restaurantAPI.getRestaurantDetails(restaurantId)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.body != null) {
                        ApiResult.Success(apiResponse.body)
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    ApiResult.Error(
                        message = response.errorBody()?.string() ?: "Failed to get restaurant details",
                        code = response.code()
                    )
                }
            } catch (e: IOException) {
                ApiResult.Error(
                    message = e.localizedMessage ?: "Network error occurred",
                    code = null
                )
            }
        }
    }

    override suspend fun getGroupRecommendations(
        groupId: String,
        userPreferences: List<UserPreferenceData>
    ): ApiResult<List<Restaurant>> {
        return withContext(Dispatchers.IO) {
            try {
                // Convert UserPreferenceData to UserPreferenceDto
                val userPreferenceDtos = userPreferences.map { pref ->
                    UserPreferenceDto(
                        cuisineTypes = pref.cuisineTypes,
                        budget = pref.budget,
                        location = LocationDto(
                            coordinates = listOf(pref.longitude, pref.latitude)
                        ),
                        radiusKm = pref.radiusKm
                    )
                }

                val request = GroupRecommendationsRequest(
                    userPreferences = userPreferenceDtos
                )

                val response = restaurantAPI.getGroupRecommendations(groupId, request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.body != null) {
                        ApiResult.Success(apiResponse.body)
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    ApiResult.Error(
                        message = response.errorBody()?.string() ?: "Failed to get recommendations",
                        code = response.code()
                    )
                }
            } catch (e: IOException) {
                ApiResult.Error(
                    message = e.localizedMessage ?: "Network error occurred",
                    code = null
                )
            }
        }
    }
}