package com.example.cpen_321.data.network.dto

import com.example.cpen_321.data.model.Restaurant
import com.google.gson.annotations.SerializedName

/**
 * Restaurant search response
 * GET /api/restaurant/search
 */
typealias RestaurantSearchResponse = List<Restaurant>

/**
 * Restaurant details response
 * GET /api/restaurant/:restaurantId
 */
typealias RestaurantDetailsResponse = Restaurant

/**
 * Group recommendations request
 * POST /api/restaurant/recommendations/:groupId
 */
data class GroupRecommendationsRequest(
    @SerializedName("userPreferences")
    val userPreferences: List<UserPreferenceDto>
)

data class UserPreferenceDto(
    @SerializedName("cuisineTypes")
    val cuisineTypes: List<String>,

    @SerializedName("budget")
    val budget: Double,

    @SerializedName("location")
    val location: LocationDto,

    @SerializedName("radiusKm")
    val radiusKm: Double
)

data class LocationDto(
    @SerializedName("coordinates")
    val coordinates: List<Double> // [longitude, latitude]
)

/**
 * Group recommendations response
 */
typealias GroupRecommendationsResponse = List<Restaurant>