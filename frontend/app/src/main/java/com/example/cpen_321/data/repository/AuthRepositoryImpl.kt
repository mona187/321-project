package com.example.cpen_321.data.repository

import com.example.cpen_321.data.local.PreferencesManager
import com.example.cpen_321.data.local.TokenManager
import com.example.cpen_321.data.network.RetrofitClient
import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.network.dto.AuthResponse
import com.example.cpen_321.data.network.dto.AuthUser
import com.example.cpen_321.data.network.dto.FcmTokenRequest
import com.example.cpen_321.data.network.dto.GoogleAuthRequest
import com.example.cpen_321.data.network.dto.MessageResponse
import com.example.cpen_321.data.network.dto.map
import com.example.cpen_321.data.network.dto.mapError
import com.example.cpen_321.data.network.safeApiCall
import com.example.cpen_321.data.network.safeAuthApiCall
import com.example.cpen_321.data.network.safeMessageApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import com.google.gson.JsonSyntaxException

/**
 * Implementation of AuthRepository
 */
class AuthRepositoryImpl(
    private val tokenManager: TokenManager,
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    private val authAPI = RetrofitClient.authAPI

    override suspend fun signUp(idToken: String): ApiResult<AuthResponse> {
        val response = safeAuthApiCall (
            authApiCall = {authAPI.signUp(GoogleAuthRequest(idToken))},
            customErrorCode = "Failed to sign up"
            )
                .also { result ->
                    // Side-effects on success
                    if (result is ApiResult.Success) {
                        val authResponse = result.data
                        tokenManager.saveToken(authResponse.token)
                        tokenManager.saveUserInfo(
                            userId = authResponse.user.userId,
                            email = authResponse.user.email,
                            googleId = "", // Not provided by backend
                            profilePicture = authResponse.user.profilePicture
                        )
                    }
                }
                .mapError { error ->
                    // Custom error message transformation
                    val errorMessage = when (error.code) {
                        409 -> "Account already exists. Please sign in instead."
                        400 -> "Invalid request. Please try again."
                        401 -> "Authentication failed. Please try again."
                        else -> error.message // Fallback to the original error message
                    }
                    // Return a new Error object with the improved message
                    ApiResult.Error(errorMessage, error.code)
                }

        return response
    }

    override suspend fun signIn(idToken: String): ApiResult<AuthResponse> {

        val response = safeAuthApiCall (
            authApiCall = {authAPI.signIn(GoogleAuthRequest(idToken))},
            customErrorCode = "Sign in failed. Please try again."
        ).also { result ->
                // Side-effects on success
                if (result is ApiResult.Success) {
                    val authResponse = result.data
                    // Save token and user info
                    tokenManager.saveToken(authResponse.token)
                    tokenManager.saveUserInfo(
                        userId = authResponse.user.userId,
                        email = authResponse.user.email,
                        googleId = "", // Backend doesn't return googleId in response
                        profilePicture = authResponse.user.profilePicture
                    )
                }
            }
            .mapError { error ->
                // Custom error message transformation
                val errorMessage = when (error.code) {
                    404 -> "Account not found. Please sign up first."
                    400 -> "Invalid request. Please try again."
                    401 -> "Authentication failed. Please try again."
                    else ->  "Sign in failed. Please try again."
                }
                // Return a new Error object with the improved message
                ApiResult.Error(errorMessage, error.code)
            }

        return response
    }

    override suspend fun googleAuth(idToken: String): ApiResult<AuthResponse> {
        return withContext(Dispatchers.IO) {

            safeAuthApiCall(
                authApiCall = { authAPI.googleAuth(GoogleAuthRequest(idToken)) },
                customErrorCode = "Authentication failed"
            ).also { response ->

                if (response is ApiResult.Success) {
                    val authResponse = response.data
                    // Save token and user info
                    tokenManager.saveToken(authResponse.token)
                    tokenManager.saveUserInfo(
                        userId = authResponse.user.userId,
                        email = authResponse.user.email,
                        googleId = "", // Backend doesn't return googleId in response
                        profilePicture = authResponse.user.profilePicture
                    )
                }
            }
        }
    }

    override suspend fun logout(): ApiResult<String> {

        // Clear local data
        clearAuthData()
        val response = safeMessageApiCall(
            messageApiCall = {authAPI.logout()},
            customErrorCode = "Logged out locally"
        ).also { result ->
            /* Data cleared no matter what */
            clearAuthData()

            if (result is ApiResult.Success) {
                // Clear local data
                ApiResult.Success("Logged out successfully")
            } else {
                // Even if server call fails, clear local data
                ApiResult.Success("Logged out locally")
            }
        }
        return response;
    }

    override suspend fun verifyToken(): ApiResult<AuthUser> {

        return withContext(Dispatchers.IO) {
            try {
                val response = authAPI.verifyToken()

                if (response.isSuccessful) {
                    val verifyResponse = response.body()
                    if (verifyResponse != null) {
                        ApiResult.Success(verifyResponse.user)
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    ApiResult.Error(
                        message = "Token verification failed",
                        code = response.code()
                    )
                }
            } catch (e: IOException) {
                ApiResult.Error("Network error: ${e.localizedMessage}")
            } catch (e: HttpException) {
                ApiResult.Error("HTTP error ${e.code()}: ${e.message()}", code = e.code())
            } catch (e: JsonSyntaxException) {
                ApiResult.Error("Parsing error: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun updateFcmToken(fcmToken: String): ApiResult<String> {
        // Save locally first
        preferencesManager.saveFcmToken(fcmToken)

        val result = safeMessageApiCall(
            messageApiCall = { authAPI.updateFcmToken(FcmTokenRequest(fcmToken)) },
            customErrorCode = "Failed to update FCM token"
        ).also { result ->
            if(result is ApiResult.Success) {
                // Save locally
                preferencesManager.saveFcmToken(fcmToken)
            }
        }.map { messageResult -> "FCM token updated successfully"}

        return result;
    }

    override suspend fun deleteAccount(): ApiResult<String> {

        clearAuthData()

        val response = safeMessageApiCall(
            messageApiCall = { authAPI.deleteAccount() },
            customErrorCode = "Failed to delete account"
        ).map{ messageResult -> "Account deleted successfully"}

        return response;
    }

    override fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    override fun getCurrentUserId(): String? {
        return tokenManager.getUserId()
    }

    override fun clearAuthData() {
        tokenManager.clearAll()
        preferencesManager.clearAll()
    }
}