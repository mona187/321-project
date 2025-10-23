package com.example.cpen_321.data.network.api

import com.example.cpen_321.data.model.Restaurant
import com.example.cpen_321.data.model.RestaurantRecommendation
import com.example.cpen_321.data.network.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RestaurantApi {
    @GET("restaurant/search")
    suspend fun searchRestaurants(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int = 5000,
        @Query("cuisineTypes") cuisineTypes: String? = null,
        @Query("priceLevel") priceLevel: Int? = null
    ): Response<ApiResponse<List<Restaurant>>>

    @GET("restaurant/{restaurantId}")
    suspend fun getRestaurantDetails(@Path("restaurantId") restaurantId: String): Response<ApiResponse<Restaurant>>

    @POST("restaurant/recommendations/{groupId}")
    suspend fun getGroupRecommendations(
        @Path("groupId") groupId: String,
        @Body userPreferences: List<UserPreference>
    ): Response<ApiResponse<List<RestaurantRecommendation>>>
}

data class UserPreference(
    val userId: String,
    val cuisine: List<String>,
    val budget: Double,
    val radiusKm: Double,
    val location: Location = Location()
)

data class Location(
    val coordinates: List<Double> = listOf(-123.1207, 49.2827) // Default to Vancouver coordinates
)
