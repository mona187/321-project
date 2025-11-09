package com.example.cpen_321.data.repository

import com.example.cpen_321.data.local.PreferencesManager
import com.example.cpen_321.data.local.TokenManager
import com.example.cpen_321.data.network.RetrofitClient
import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.network.dto.AuthResponse
import com.example.cpen_321.data.network.dto.AuthUser
import com.example.cpen_321.data.network.dto.FcmTokenRequest
import com.example.cpen_321.data.network.dto.GoogleAuthRequest
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
        return withContext(Dispatchers.IO) {
            try {
                val response = authAPI.signUp(GoogleAuthRequest(idToken))

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        // Save token and user info
                        tokenManager.saveToken(authResponse.token)
                        tokenManager.saveUserInfo(
                            userId = authResponse.user.userId,
                            email = authResponse.user.email,
                            googleId = "", // Backend doesn't return googleId in response
                            profilePicture = authResponse.user.profilePicture
                        )

                        ApiResult.Success(authResponse)
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = when (response.code()) {
                        409 -> "Account already exists. Please sign in instead."
                        400 -> "Invalid request. Please try again."
                        401 -> "Authentication failed. Please try again."
                        else -> errorBody ?: "Sign up failed. Please try again."
                    }
                    ApiResult.Error(
                        message = errorMessage,
                        code = response.code()
                    )
                }
            } catch (e: IOException) {
                ApiResult.Error("Network error: ${e.localizedMessage}")
            } catch (e: HttpException) {
                ApiResult.Error("HTTP error ${e.code()}: ${e.message()}", code = e.code())
            } catch (e: JsonSyntaxException) {
                ApiResult.Error("Parsing error: ${e.localizedMessage}")
            } catch (e: Exception) {
                ApiResult.Error("Unexpected error: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun signIn(idToken: String): ApiResult<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authAPI.signIn(GoogleAuthRequest(idToken))

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        // Save token and user info
                        tokenManager.saveToken(authResponse.token)
                        tokenManager.saveUserInfo(
                            userId = authResponse.user.userId,
                            email = authResponse.user.email,
                            googleId = "", // Backend doesn't return googleId in response
                            profilePicture = authResponse.user.profilePicture
                        )

                        ApiResult.Success(authResponse)
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = when (response.code()) {
                        404 -> "Account not found. Please sign up first."
                        400 -> "Invalid request. Please try again."
                        401 -> "Authentication failed. Please try again."
                        else -> errorBody ?: "Sign in failed. Please try again."
                    }
                    ApiResult.Error(
                        message = errorMessage,
                        code = response.code()
                    )
                }
            } catch (e: IOException) {
                ApiResult.Error("Network error: ${e.localizedMessage}")
            } catch (e: HttpException) {
                ApiResult.Error("HTTP error ${e.code()}: ${e.message()}", code = e.code())
            } catch (e: JsonSyntaxException) {
                ApiResult.Error("Parsing error: ${e.localizedMessage}")
            } catch (e: Exception) {
                ApiResult.Error("Unexpected error: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun googleAuth(idToken: String): ApiResult<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authAPI.googleAuth(GoogleAuthRequest(idToken))

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        // Save token and user info
                        tokenManager.saveToken(authResponse.token)
                        tokenManager.saveUserInfo(
                            userId = authResponse.user.userId,
                            email = authResponse.user.email,
                            googleId = "", // Backend doesn't return googleId in response
                            profilePicture = authResponse.user.profilePicture
                        )

                        ApiResult.Success(authResponse)
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    ApiResult.Error(
                        message = errorBody ?: "Authentication failed",
                        code = response.code()
                    )
                }
            } catch (e: IOException) {
                ApiResult.Error("Network error: ${e.localizedMessage}")
            } catch (e: HttpException) {
                ApiResult.Error("HTTP error ${e.code()}: ${e.message()}", code = e.code())
            } catch (e: JsonSyntaxException) {
                ApiResult.Error("Parsing error: ${e.localizedMessage}")
            } catch (e: Exception) {
                ApiResult.Error("Unexpected error: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun logout(): ApiResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authAPI.logout()

                if (response.isSuccessful) {
                    // Clear local data
                    clearAuthData()
                    ApiResult.Success("Logged out successfully")
                } else {
                    // Even if server call fails, clear local data
                    clearAuthData()
                    ApiResult.Success("Logged out locally")
                }
            } catch (e: IOException) {
                ApiResult.Error("Network error: ${e.localizedMessage}")
            } catch (e: HttpException) {
                ApiResult.Error("HTTP error ${e.code()}: ${e.message()}", code = e.code())
            } catch (e: JsonSyntaxException) {
                ApiResult.Error("Parsing error: ${e.localizedMessage}")
            } catch (e: Exception) {
                ApiResult.Error("Unexpected error: ${e.localizedMessage}")
            }
        }
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
            } catch (e: Exception) {
                ApiResult.Error("Unexpected error: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun updateFcmToken(fcmToken: String): ApiResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Save locally first
                preferencesManager.saveFcmToken(fcmToken)

                val response = authAPI.updateFcmToken(FcmTokenRequest(fcmToken))

                if (response.isSuccessful) {
                    ApiResult.Success("FCM token updated successfully")
                } else {
                    ApiResult.Error(
                        message = "Failed to update FCM token",
                        code = response.code()
                    )
                }
            } catch (e: IOException) {
                ApiResult.Error("Network error: ${e.localizedMessage}")
            } catch (e: HttpException) {
                ApiResult.Error("HTTP error ${e.code()}: ${e.message()}", code = e.code())
            } catch (e: JsonSyntaxException) {
                ApiResult.Error("Parsing error: ${e.localizedMessage}")
            } catch (e: Exception) {
                ApiResult.Error("Unexpected error: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun deleteAccount(): ApiResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authAPI.deleteAccount()

                if (response.isSuccessful) {
                    // Clear local data
                    clearAuthData()
                    ApiResult.Success("Account deleted successfully")
                } else {
                    val errorBody = response.errorBody()?.string()
                    ApiResult.Error(
                        message = errorBody ?: "Failed to delete account",
                        code = response.code()
                    )
                }
            } catch (e: IOException) {
                ApiResult.Error("Network error: ${e.localizedMessage}")
            } catch (e: HttpException) {
                ApiResult.Error("HTTP error ${e.code()}: ${e.message()}", code = e.code())
            } catch (e: JsonSyntaxException) {
                ApiResult.Error("Parsing error: ${e.localizedMessage}")
            } catch (e: Exception) {
                ApiResult.Error("Unexpected error: ${e.localizedMessage}")
            }
        }
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