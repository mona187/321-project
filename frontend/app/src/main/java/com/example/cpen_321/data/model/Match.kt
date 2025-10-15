package com.example.cpen_321.data.model

data class WaitingRoomState(
    val roomId: String? = null,
    val members: List<UserProfile> = emptyList(),
    val completionTime: Int = 600,
    val groupReady: Boolean = false
)