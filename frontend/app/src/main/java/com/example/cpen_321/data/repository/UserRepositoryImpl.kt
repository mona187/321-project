package com.example.cpen_321.data.repository

import com.example.cpen_321.data.local.PreferencesManager
import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.model.UserSettings
import com.example.cpen_321.data.network.api.UserAPI
import com.example.cpen_321.data.network.dto.ApiResponse
import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.network.dto.UpdateProfileRequest
import com.example.cpen_321.data.network.dto.UpdateSettingsRequest
import com.example.cpen_321.data.network.dto.map
import com.example.cpen_321.data.network.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import com.google.gson.JsonSyntaxException
import retrofit2.Response
import javax.inject.Inject

/**
 * Implementation of UserRepository
 */
class UserRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val userAPI: UserAPI
) : UserRepository {

    override suspend fun getUserProfiles(userIds: List<String>): ApiResult<List<UserProfile>> {
        val idsString = userIds.joinToString(",")
        val response = safeApiCall( apiCall ={userAPI.getUserProfiles(idsString)}, customErrorCode = "Failed to fetch profiles")
        return response;
    }

    override suspend fun getUserSettings(): ApiResult<UserSettings> {

        val apiResult = safeApiCall(
            apiCall = { userAPI.getUserSettings() },
            customErrorCode = "Failed to fetch settings"
        )
        if (apiResult is ApiResult.Success) {
            val settings = apiResult.data
            // Save preferences locally
            preferencesManager.saveCuisines(settings.preference.toSet())
            preferencesManager.saveBudget(settings.budget)
            preferencesManager.saveRadius(settings.radiusKm)
        }
        return apiResult
    }

    override suspend fun updateUserProfile(
        name: String?,
        bio: String?,
        profilePicture: String?,
        contactNumber: String?
    ): ApiResult<UserProfile> {

        val request = UpdateProfileRequest(
            name = name,
            bio = bio,
            profilePicture = profilePicture,
            contactNumber = contactNumber
        )

        val apiResult = safeApiCall(
            apiCall = { userAPI.updateUserProfile(request) },
            customErrorCode = "Failed to update profile"
        )

        return apiResult
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

        val request = UpdateSettingsRequest(
            name = name,
            bio = bio,
            preference = preference,
            profilePicture = profilePicture,
            contactNumber = contactNumber,
            budget = budget,
            radiusKm = radiusKm
        )

        val apiResult = safeApiCall(apiCall = {userAPI.updateUserSettings(request)}, customErrorCode = "Failed to update settings")

        if (apiResult is ApiResult.Success) {
            val settings = apiResult.data

            preferencesManager.saveCuisines(settings.preference.toSet())
            preferencesManager.saveBudget(settings.budget)
            preferencesManager.saveRadius(settings.radiusKm)
        }

        return apiResult
    }

    override suspend fun deleteUser(userId: String): ApiResult<Boolean> {
        /* map makes success api.body.deleted */
        return safeApiCall(
            apiCall = { userAPI.deleteUser(userId) },
            customErrorCode = "Failed to delete user"
        ).map { it.deleted }
    }
}