package com.example.cpen_321.data.network
import com.example.cpen_321.data.network.dto.ApiResponse
import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.network.dto.AuthResponse
import com.example.cpen_321.data.network.dto.Message
import com.example.cpen_321.data.network.dto.MessageResponse
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * A utility function to wrap repository API calls for robust error handling.
 *
 * It executes the suspend block [apiCall] in the IO dispatcher and handles exceptions.
 * The result is mapped to the [ApiResult] sealed class. It also inspects the custom
 * [ApiResponse] wrapper to determine success or failure.
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<ApiResponse<T>>, customErrorCode: String? = null): ApiResult<T> {
    // Switch to the IO thread for network operations
    return withContext(Dispatchers.IO) {
        try {
            // Invoke the suspend lambda to make the network request
            val response = apiCall()

            if (response.isSuccessful) {
                val apiResponse = response.body()
                // Check if the custom API response body is not null and the body field within it exists
                if (apiResponse != null && apiResponse.body != null) {
                    // You might also want to check your custom status code, e.g., apiResponse.status == 200
                    if (!apiResponse.message.isError()) {
                        ApiResult.Success(apiResponse.body)
                    } else {
                        // The server responded with a 2xx HTTP status but indicated an error in the response body
                        ApiResult.Error(apiResponse.message.getDisplayMessage(), apiResponse.status)
                    }
                } else {
                    if (customErrorCode == null) {
                        // The server responded with a 2xx HTTP status but the body was null or malformed
                        ApiResult.Error("Empty or malformed response from server", response.code())
                    } else {
                        ApiResult.Error(customErrorCode, response.code())
                    }
                }
            } else {
                // The server responded with a non-2xx HTTP status code (e.g., 404, 500)
                ApiResult.Error(
                    message = response.errorBody()?.string() ?: "Failed to make request",
                    code = response.code()
                )
            }
        } catch (e: IOException) {
            // Handle network connectivity issues
            ApiResult.Error("Network error: ${e.localizedMessage}", code = null)
        } catch (e: HttpException) {
            // Handle non-2xx HTTP status codes that throw exceptions
            ApiResult.Error("HTTP error ${e.code()}: ${e.message()}", code = e.code())
        } catch (e: JsonSyntaxException) {
            // Handle errors in parsing the JSON response
            ApiResult.Error("Parsing error: ${e.localizedMessage}", code = null)
        } catch (e: Exception) {
            // Handle any other unexpected exceptions
            ApiResult.Error("An unexpected error occurred: ${e.localizedMessage}", code = null)
        }
    }
}

/**
 * A utility function to wrap repository API calls for robust error handling.
 *
 * It executes the suspend block [authApiCall] in the IO dispatcher and handles exceptions.
 * The result is mapped to the [ApiResult] sealed class. It also inspects the custom
 * [ApiResponse] wrapper to determine success or failure.
 *
 * This is a modification of the above function, this is used in the AuthRepositoryImpl code.
 */
suspend fun safeAuthApiCall(authApiCall: suspend () -> Response<AuthResponse>, customErrorCode: String? = null): ApiResult<AuthResponse> {
    // Switch to the IO thread for network operations
    return withContext(Dispatchers.IO) {
        try {
            // Invoke the suspend lambda to make the network request
            val response = authApiCall()

            if (response.isSuccessful) {
                val apiResponse = response.body()
                // Check if the custom API response body is not null and the body field within it exists
                if (apiResponse != null) {
                    // You might also want to check your custom status code, e.g., apiResponse.status == 200
                    ApiResult.Success(apiResponse)
                } else {
                    if (customErrorCode == null) {
                        // The server responded with a 2xx HTTP status but the body was null or malformed
                        ApiResult.Error("Empty or malformed response from server", response.code())
                    } else {
                        ApiResult.Error(customErrorCode, response.code())
                    }
                }
            } else {
                // The server responded with a non-2xx HTTP status code (e.g., 404, 500)
                ApiResult.Error(
                    message = response.errorBody()?.string() ?: "Failed to make request",
                    code = response.code()
                )
            }
        } catch (e: IOException) {
            // Handle network connectivity issues
            ApiResult.Error("Network error: ${e.localizedMessage}", code = null)
        } catch (e: HttpException) {
            // Handle non-2xx HTTP status codes that throw exceptions
            ApiResult.Error("HTTP error ${e.code()}: ${e.message()}", code = e.code())
        } catch (e: JsonSyntaxException) {
            // Handle errors in parsing the JSON response
            ApiResult.Error("Parsing error: ${e.localizedMessage}", code = null)
        } catch (e: Exception) {
            // Handle any other unexpected exceptions
            ApiResult.Error("An unexpected error occurred: ${e.localizedMessage}", code = null)
        }
    }
}

/**
 * A utility function to wrap repository API calls for robust error handling.
 *
 * It executes the suspend block [messageApiCall] in the IO dispatcher and handles exceptions.
 * The result is mapped to the [ApiResult] sealed class. It also inspects the custom
 * [ApiResponse] wrapper to determine success or failure.
 *
 * This is a modification of the above function, this is used in the AuthRepositoryImpl code.
 */
suspend fun safeMessageApiCall(messageApiCall: suspend () -> Response<MessageResponse>, customErrorCode: String? = null): ApiResult<String> {
    // Switch to the IO thread for network operations
    return withContext(Dispatchers.IO) {
        try {
            val response = messageApiCall()

            if (response.isSuccessful) {
                /* overwrite this */
                ApiResult.Success(response.message())
            } else {
                if (customErrorCode == null) {
                    ApiResult.Error(
                        message = "Failed to make request",
                        code = response.code()
                    )
                } else {
                    ApiResult.Error(
                        message = customErrorCode,
                        code = response.code()
                    )

                }
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
