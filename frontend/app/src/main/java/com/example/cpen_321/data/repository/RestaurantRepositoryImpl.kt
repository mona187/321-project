package com.example.cpen_321.data.repository

import com.example.cpen_321.data.model.Restaurant
import com.example.cpen_321.data.network.RetrofitClient
import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.network.dto.GroupRecommendationsRequest
import com.example.cpen_321.data.network.dto.LocationDto
import com.example.cpen_321.data.network.dto.UserPreferenceDto
import com.example.cpen_321.data.network.safeApiCall
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
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
        // Convert cuisineTypes list to comma-separated string
        val cuisineTypesString = cuisineTypes?.joinToString(",")

        val response = safeApiCall(
            apiCall = {restaurantAPI.searchRestaurants(
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                cuisineTypes = cuisineTypesString,
                priceLevel = priceLevel
            )},
            customErrorCode = "Failed to search restaurants"
        )
        return response;
    }

    override suspend fun getRestaurantDetails(restaurantId: String): ApiResult<Restaurant> {

        val response = safeApiCall(
            apiCall = {restaurantAPI.getRestaurantDetails(restaurantId)},
            customErrorCode = "Failed to get restaurant details"
        )
        return response;

    }

    override suspend fun getGroupRecommendations(
        groupId: String,
        userPreferences: List<UserPreferenceData>
    ): ApiResult<List<Restaurant>> {

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

        val response =safeApiCall(
         apiCall = {restaurantAPI.getGroupRecommendations(groupId, request)},
            customErrorCode = "Failed to get recommendations"
        )

        return response;
    }
}