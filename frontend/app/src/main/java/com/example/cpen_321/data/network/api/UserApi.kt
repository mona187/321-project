package com.example.cpen_321.data.network.api

import com.example.cpen_321.data.model.UserProfile
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {
    @GET("/api/user/profile")
    suspend fun getUserProfiles(@Query("ids") ids: List<Int>): Response<List<UserProfile>>
}
