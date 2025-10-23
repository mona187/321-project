package com.example.cpen_321.data.network.api

import com.example.cpen_321.data.model.UserProfile
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.cpen_321.data.model.User
import retrofit2.http.PUT
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.cpen_321.data.network.dto.ApiResponse

interface UserApi {
    @GET("user/profile")
    suspend fun getUserProfiles(@Query("ids") ids: String): Response<List<UserProfile>>

    @GET("user/settings")
    suspend fun getUserSettings(): Response<ApiResponse<User>>

    @PUT("user/settings")
    suspend fun updateUserSettings(@Body userSettings: User): Response<ApiResponse<User>>

    @POST("user/settings")
    suspend fun createUserSettings(@Body userSettings: User): Response<ApiResponse<User>>
}
