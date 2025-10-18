package com.example.cpen_321.data.network.api

import com.example.cpen_321.data.network.dto.ApiResponse
import com.example.cpen_321.data.network.dto.AuthData

import com.example.cpen_321.data.network.dto.GoogleSigninRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/signin")
    suspend fun googleSignIn (@Body request: GoogleSigninRequest): Response<ApiResponse<AuthData>>
    @POST("auth/signup")
    suspend fun googleSignUp(@Body request: GoogleSigninRequest): Response<ApiResponse<AuthData>>
}