package com.example.cpen_321.data.network.api

import com.example.cpen_321.data.model.UserProfile
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.cpen_321.data.model.User
import retrofit2.http.PUT
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {
    @GET("/api/user/profile")
    suspend fun getUserProfiles(@Query("ids") ids: List<Int>): Response<List<UserProfile>>

    @GET("/api/user/settings")
    suspend fun getUserSettings(): Response<User>

    @PUT("/api/user/settings")
    suspend fun updateUserSettings(@Body userSettings: User): Response<User>

    @POST("/api/user/settings")
    suspend fun createUserSettings(@Body userSettings: User): Response<User>
}
