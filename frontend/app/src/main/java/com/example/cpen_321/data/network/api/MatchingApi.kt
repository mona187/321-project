package com.example.cpen_321.data.network.api

import com.example.cpen_321.data.network.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MatchingApi {
    @POST("matching/join")
    suspend fun joinMatching(@Body request: JoinMatchingRequest): Response<ApiResponse<JoinMatchingResponse>>
}
