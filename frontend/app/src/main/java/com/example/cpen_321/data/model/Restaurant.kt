package com.example.cpen_321.data.model

import com.google.gson.annotations.SerializedName

/**
 * Restaurant model matching backend specification
 */
data class Restaurant(
    @SerializedName("restaurantId")
    val restaurantId: String? = null,

    @SerializedName("name")
    val name: String,

    @SerializedName("location")
    val location: String,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("priceLevel")
    val priceLevel: Int? = null,

    @SerializedName("rating")
    val rating: Double? = null,

    @SerializedName("photos")
    val photos: List<String>? = null,

    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,

    @SerializedName("website")
    val website: String? = null,

    @SerializedName("url")
    val url: String? = null,

    @SerializedName("cuisine")
    val cuisine: String? = null,

    @SerializedName("priceRange")
    val priceRange: String? = null
) {
    /**
     * Get price level as dollar signs ($, $$, $$$, $$$$)
     */
    fun getPriceLevelString(): String {
        return when (priceLevel) {
            1 -> "$"
            2 -> "$$"
            3 -> "$$$"
            4 -> "$$$$"
            else -> "N/A"
        }
    }

    /**
     * Get rating as string with 1 decimal
     */
    fun getRatingString(): String {
        return rating?.let { "%.1f".format(it) } ?: "N/A"
    }

    /**
     * Get first photo URL or null
     */
    fun getMainPhotoUrl(): String? {
        return photos?.firstOrNull()
    }
}

/**
 * Restaurant search parameters
 */
data class RestaurantSearchParams(
    val latitude: Double,
    val longitude: Double,
    val radius: Int = 5000, // in meters
    val cuisineTypes: List<String>? = null,
    val priceLevel: Int? = null
)

/**
 * Restaurant recommendation request for a group
 */
data class RestaurantRecommendationRequest(
    @SerializedName("userPreferences")
    val userPreferences: List<UserPreference>
)

data class UserPreference(
    @SerializedName("cuisineTypes")
    val cuisineTypes: List<String>,

    @SerializedName("budget")
    val budget: Double,

    @SerializedName("location")
    val location: Location,

    @SerializedName("radiusKm")
    val radiusKm: Double
)

data class Location(
    @SerializedName("coordinates")
    val coordinates: List<Double> // [longitude, latitude]
)