package com.example.cpen_321.data.model

data class Restaurant(
    val name: String,
    val location: String,
    val restaurantId: String,
    val address: String,
    val priceLevel: Int,
    val rating: Double,
    val photos: List<String>,
    val phoneNumber: String? = null,
    val website: String? = null,
    val url: String? = null
)

data class RestaurantRecommendation(
    val restaurant: Restaurant,
    val score: Double,
    val reasons: List<String>
)
