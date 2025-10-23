package com.example.cpen_321.data.network.dto


data class ValidationError(
    val field: String,
    val message:String

)
data class ApiResponse<T>(
    val message: String,
    val data: T? = null,

    // For error responses
    val error: String? = null,
    val details: List<ValidationError>? = null

)




