package com.example.cpen_321.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen_321.data.model.User
import com.example.cpen_321.data.network.dto.AuthData
import com.example.cpen_321.data.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ------------------------- UI State -------------------------

data class AuthUiState(
    val isSigningIn: Boolean = false,
    val isSigningUp: Boolean = false,
    val isCheckingAuth: Boolean = true,
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val requiresProfileSetup: Boolean = false,
    )

// ------------------------- ViewModel -------------------------

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthenticationStatus()
    }

    // ------------------------- AUTH CHECK -------------------------

    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isCheckingAuth = true)
                val isAuthenticated = authRepository.isUserAuthenticated()
                val user = if (isAuthenticated) authRepository.getCurrentUser() else null
                val needsSetup = user?.bio.isNullOrBlank() || user?.contactNumber.isNullOrBlank() || user?.preference.isNullOrEmpty()

                _uiState.value = _uiState.value.copy(
                    isAuthenticated = isAuthenticated,
                    user = user,
                    requiresProfileSetup = needsSetup,
                    isCheckingAuth = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "Authentication check failed", e)
                _uiState.value = _uiState.value.copy(
                    isCheckingAuth = false,
                    isAuthenticated = false,
                    errorMessage = "Error checking authentication: ${e.message}"
                )
            }
        }
    }

    // ------------------------- GOOGLE SIGN-IN -------------------------

    suspend fun signInWithGoogle(context: Context): Result<GoogleIdTokenCredential> {
        return authRepository.signInWithGoogle(context)
    }

    fun handleGoogleSignInResult(credential: GoogleIdTokenCredential) {
        handleGoogleAuthResult(credential, isSignUp = false) { idToken ->
            authRepository.googleSignIn(idToken)
        }
    }

    fun handleGoogleSignUpResult(credential: GoogleIdTokenCredential) {
        handleGoogleAuthResult(credential, isSignUp = true) { idToken ->
            authRepository.googleSignUp(idToken)
        }
    }

    private fun handleGoogleAuthResult(
        credential: GoogleIdTokenCredential,
        isSignUp: Boolean,
        authOperation: suspend (String) -> Result<AuthData>
    ) {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isSigningIn = !isSignUp,
                isSigningUp = isSignUp
            )

            try {
                authOperation(credential.idToken)
                    .onSuccess { authData ->
                        val user = authData.user
                        // ✅ New: Determine if profile setup is needed
                        val needsSetup = user.bio.isNullOrBlank() ||
                                user.contactNumber.isNullOrBlank() ||
                                user.preference.isNullOrEmpty()

                        _uiState.value = _uiState.value.copy(
                            isSigningIn = false,
                            isSigningUp = false,
                            isAuthenticated = true,
                            requiresProfileSetup = needsSetup,
                            user = user,
                            successMessage = if (isSignUp)
                                "Account created successfully!"
                            else
                                "Signed in successfully!"
                        )
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Google ${if (isSignUp) "sign-up" else "sign-in"} failed", error)
                        _uiState.value = _uiState.value.copy(
                            isSigningIn = false,
                            isSigningUp = false,
                            errorMessage = error.message
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error during Google auth result handling", e)
                _uiState.value = _uiState.value.copy(
                    isSigningIn = false,
                    isSigningUp = false,
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    // ------------------------- SIGN OUT -------------------------

    fun signOut() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting sign out process...")
                authRepository.clearToken()
                Log.d(TAG, "Token cleared successfully")
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = false,
                    user = null,
                    requiresProfileSetup = false,
                    isSigningIn = false,
                    isSigningUp = false,
                    errorMessage = null,
                    successMessage = null
                )
                Log.d(TAG, "UI state updated - isAuthenticated: ${_uiState.value.isAuthenticated}")
            } catch (e: Exception) {
                Log.e(TAG, "Error during sign out", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error signing out: ${e.message}"
                )
            }
        }
    }

    // ------------------------- UI MESSAGE HELPERS -------------------------

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun setSuccessMessage(message: String) {
        _uiState.value = _uiState.value.copy(successMessage = message)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
