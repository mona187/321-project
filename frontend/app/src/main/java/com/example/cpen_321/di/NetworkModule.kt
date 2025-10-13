//package com.example.cpen_321.di
//
//import com.example.cpen_321.data.network.api.AuthApiService
//import com.example.cpen_321.data.network.AuthInterceptor
//import com.example.cpen_321.data.network.api.RetrofitClient
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object NetworkModule {
//
//    @Provides
//    @Singleton
//    fun provideAuthInterceptor(): AuthInterceptor {
//        return AuthInterceptor()
//    }
//
//    @Provides
//    @Singleton
//    fun provideAuthApiService(): AuthApiService {
//        return RetrofitClient.retrofit.create(AuthApiService::class.java)
//    }
//}
