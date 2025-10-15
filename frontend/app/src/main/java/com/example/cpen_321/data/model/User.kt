package com.example.cpen_321.data.model

data class UserProfile (
    val userId: String,
    val name: String,
    val bio: String,
    val profilePicture: String? = null
)

