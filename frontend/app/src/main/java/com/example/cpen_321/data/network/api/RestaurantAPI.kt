package com.example.cpen_321.data.network.api

import com.example.cpen_321.data.model.Restaurant
import com.example.cpen_321.data.network.dto.ApiResponse
import com.example.cpen_321.data.network.dto.GroupRecommendationsRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Restaurant API endpoints
 */
interface RestaurantAPI {

    /**
     * GET /api/restaurant/search
     * Search for restaurants near a location
     */
    @GET("api/restaurant/search")
    suspend fun searchRestaurants(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int? = null,
        @Query("cuisineTypes") cuisineTypes: String? = null, // comma-separated
        @Query("priceLevel") priceLevel: Int? = null
    ): Response<ApiResponse<List<Restaurant>>>

    /**
     * GET /api/restaurant/:restaurantId
     * Get restaurant details by ID
     */
    @GET("api/restaurant/{restaurantId}")
    suspend fun getRestaurantDetails(
        @Path("restaurantId") restaurantId: String
    ): Response<ApiResponse<Restaurant>>

    /**
     * POST /api/restaurant/recommendations/:groupId
     * Get restaurant recommendations for a group
     */
    @POST("api/restaurant/recommendations/{groupId}")
    suspend fun getGroupRecommendations(
        @Path("groupId") groupId: String,
        @Body request: GroupRecommendationsRequest
    ): Response<ApiResponse<List<Restaurant>>>
}