package com.example.cpen_321.data.model

data class WaitingRoomState(
    val roomId: String? = null,
    val members: List<UserProfile> = emptyList(),
    val status: String = "waiting",
    val expiresAt: String? = null,
    val groupReady: Boolean = false,
    val timeRemainingSeconds: Int = 0 // will be computed
)