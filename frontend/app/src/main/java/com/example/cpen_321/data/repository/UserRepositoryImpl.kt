package com.example.cpen_321.data.repository

import com.example.cpen_321.data.local.PreferencesManager
import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.model.UserSettings
import com.example.cpen_321.data.network.RetrofitClient
import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.network.dto.UpdateProfileRequest
import com.example.cpen_321.data.network.dto.UpdateSettingsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import com.google.gson.JsonSyntaxException

/**
 * Implementation of UserRepository
 */
class UserRepositoryImpl(
    private val preferencesManager: PreferencesManager
) : UserRepository {

    private val userAPI = RetrofitClient.userAPI

    override suspend fun getUserProfiles(userIds: List<String>): ApiResult<List<UserProfile>> {
        return withContext(Dispatchers.IO) {
            try {
                val idsString = userIds.joinToString(",")
                val response = userAPI.getUserProfiles(idsString)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.body != null) {
                        ApiResult.Success(apiResponse.body)
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    ApiResult.Error(
                        message = response.errorBody()?.string() ?: "Failed to fetch profiles",
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

    override suspend fun getUserSettings(): ApiResult<UserSettings> {
        return withContext(Dispatchers.IO) {
            try {
                val response = userAPI.getUserSettings()

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.body != null) {
                        // Save preferences locally
                        apiResponse.body.let { settings ->
                            preferencesManager.saveCuisines(settings.preference.toSet())
                            preferencesManager.saveBudget(settings.budget)
                            preferencesManager.saveRadius(settings.radiusKm)
                        }

                        ApiResult.Success(apiResponse.body)
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    ApiResult.Error(
                        message = response.errorBody()?.string() ?: "Failed to fetch settings",
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

    override suspend fun updateUserProfile(
        name: String?,
        bio: String?,
        profilePicture: String?,
        contactNumber: String?
    ): ApiResult<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateProfileRequest(
                    name = name,
                    bio = bio,
                    profilePicture = profilePicture,
                    contactNumber = contactNumber
                )

                val response = userAPI.updateUserProfile(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.body != null) {
                        ApiResult.Success(apiResponse.body)
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    ApiResult.Error(
                        message = response.errorBody()?.string() ?: "Failed to update profile",
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

    override suspend fun updateUserSettings(
        name: String?,
        bio: String?,
        preference: List<String>?,
        profilePicture: String?,
        contactNumber: String?,
        budget: Double?,
        radiusKm: Double?
    ): ApiResult<UserSettings> {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateSettingsRequest(
                    name = name,
                    bio = bio,
                    preference = preference,
                    profilePicture = profilePicture,
                    contactNumber = contactNumber,
                    budget = budget,
                    radiusKm = radiusKm
                )

                val response = userAPI.updateUserSettings(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.body != null) {
                        // Update local preferences
                        apiResponse.body.let { settings ->
                            preferencesManager.saveCuisines(settings.preference.toSet())
                            preferencesManager.saveBudget(settings.budget)
                            preferencesManager.saveRadius(settings.radiusKm)
                        }

                        ApiResult.Success(apiResponse.body)
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    ApiResult.Error(
                        message = response.errorBody()?.string() ?: "Failed to update settings",
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

    override suspend fun deleteUser(userId: String): ApiResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = userAPI.deleteUser(userId)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.body != null) {
                        ApiResult.Success(apiResponse.body.deleted)
                    } else {
                        ApiResult.Error("Empty response from server")
                    }
                } else {
                    ApiResult.Error(
                        message = response.errorBody()?.string() ?: "Failed to delete user",
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
}