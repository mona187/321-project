package com.example.cpen_321.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen_321.data.local.TokenManager
import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.network.dto.AuthUser
import com.example.cpen_321.data.repository.AuthRepository
import com.example.cpen_321.utils.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for authentication
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val socketManager: SocketManager,
    private val tokenManager: TokenManager  // ✅ ADDED: Inject TokenManager
) : ViewModel() {

    // Authentication state
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Current user
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // REMOVE THIS - Don't auto-check on init since SplashScreen will handle it
    // init {
    //     checkAuthStatus()
    // }

    /**
     * Check if user is logged in (has token)
     * PUBLIC - Used by SplashScreen
     */
    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }

    /**
     * Verify current token with backend
     * PUBLIC - Used by SplashScreen
     */
    suspend fun verifyToken(): ApiResult<AuthUser> {
        val result = authRepository.verifyToken()

        when (result) {
            is ApiResult.Success -> {
                _currentUser.value = result.data
                _authState.value = AuthState.Authenticated

                // ✅ FIXED: Connect to socket with JWT token, not userId
                if (!socketManager.isConnected()) {
                    val token = tokenManager.getToken()  // Get the JWT token
                    if (token != null) {
                        socketManager.connect(token)  // Pass the token, not userId!
                    }
                }
            }
            is ApiResult.Error -> {
                // Token is invalid
                _authState.value = AuthState.Unauthenticated
                _currentUser.value = null
            }
            is ApiResult.Loading -> {
                // Ignore
            }
        }

        return result
    }

    /**
     * Clear authentication data
     * PUBLIC - Used by SplashScreen
     */
    fun clearAuthData() {
        authRepository.clearAuthData()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
        socketManager.disconnect()
    }

    /**
     * Authenticate with Google ID token
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = authRepository.googleAuth(idToken)) {
                is ApiResult.Success -> {
                    _currentUser.value = result.data.user
                    _authState.value = AuthState.Authenticated

                    // ✅ CORRECT: Connect to socket with token
                    socketManager.connect(result.data.token)

                    _errorMessage.value = null
                }
                is ApiResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                    _errorMessage.value = result.message
                }
                is ApiResult.Loading -> {
                    // Already handled by _isLoading
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Logout user
     */
    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true

            // Disconnect socket
            socketManager.disconnect()

            // Logout from backend
            authRepository.logout()

            // Update state
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
            _isLoading.value = false
        }
    }

    /**
     * Update FCM token for push notifications
     */
    fun updateFcmToken(fcmToken: String) {
        viewModelScope.launch {
            authRepository.updateFcmToken(fcmToken)
        }
    }

    /**
     * Delete user account
     */
    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = authRepository.deleteAccount()) {
                is ApiResult.Success -> {
                    // Disconnect socket
                    socketManager.disconnect()

                    // Clear state
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated

                    onSuccess()
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
                is ApiResult.Loading -> {
                    // Ignore
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }
}

/**
 * Authentication state sealed class
 */
sealed class AuthState {
    object Initial : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}