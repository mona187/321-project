package com.example.cpen_321.data.network.api

data class JoinMatchingRequest(
    val budget: Double,
    val cuisine: List<String>,
    val radiusKm: Double
)
