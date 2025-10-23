package com.example.cpen_321.data.network.api

data class JoinMatchingResponse(
    val roomId: String,
    val room: RoomDetails
)

data class RoomDetails(
    val roomId: String,
    val completionTime: String,
    val maxMembers: Int,
    val members: List<String>,
    val status: String,
    val cuisine: String,
    val averageBudget: Double,
    val averageRadius: Double,
    val createdAt: String,
    val updatedAt: String,
    val id: String
)
