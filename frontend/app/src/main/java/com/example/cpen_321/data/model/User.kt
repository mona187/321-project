package com.example.cpen_321.data.model

data class User(
    val userId: Int,
    val name: String,
    val bio: String?,
    val preference: String?,
    val profilePicture: String?,
    val credibilityScore: Double?,
    val contactNumber: String?,
    val budget: Double?,
    val radiusKm: Double?,
    val status: Int?,        // internal only
    val roomId: String?,
    val groupId: String?
)
data class UserProfile (
    val userId: Int,
    val name: String,
    val bio: String,
    val profilePicture: String? = null
)








