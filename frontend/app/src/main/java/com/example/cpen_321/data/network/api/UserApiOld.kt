//package com.example.cpen_321.data.network.api
//import okhttp3.MultipartBody
//import retrofit2.Response
//import retrofit2.http.Body
//import retrofit2.http.DELETE
//import retrofit2.http.GET
//import retrofit2.http.Header
//import retrofit2.http.Multipart
//import retrofit2.http.POST
//import retrofit2.http.Part
//import com.example.cpen_321.data.network.dto.ApiResponse
//    interface UserApi {
//        @GET("user/profile")
//        suspend fun getProfile(@Header("Authorization") authHeader: String): Response<ApiResponse<ProfileData>>
//
//        @POST("user/profile")
//        suspend fun updateProfile(
//            @Header("Authorization") authHeader: String,
//            @Body request: UpdateProfileRequest
//        ): Response<ApiResponse<ProfileData>>
//    }
//
////later for profile this us from m1