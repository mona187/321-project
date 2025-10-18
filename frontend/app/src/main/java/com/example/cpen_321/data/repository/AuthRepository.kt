package com.example.cpen_321.data.repository

import android.content.Context
import com.example.cpen_321.data.model.User
import com.example.cpen_321.data.network.dto.AuthData

import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential


interface AuthRepository {
    suspend fun signInWithGoogle(context: Context): Result<GoogleIdTokenCredential>
    suspend fun googleSignIn(tokenId: String): Result<AuthData>
    suspend fun googleSignUp(tokenId: String): Result<AuthData>
    suspend fun clearToken(): Result<Unit>
    suspend fun doesTokenExist(): Boolean
    suspend fun getStoredToken(): String?
    suspend fun getCurrentUser(): User?
    suspend fun isUserAuthenticated(): Boolean
}