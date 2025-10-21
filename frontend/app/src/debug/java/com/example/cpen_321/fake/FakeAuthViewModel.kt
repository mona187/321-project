package com.example.cpen_321.fake

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.example.cpen_321.ui.viewmodels.AuthUiState
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthViewModel : ViewModel() {

    // Start as "authenticated but not set up"
    private val _uiState = MutableStateFlow(
        AuthUiState(
            isAuthenticated = true,
            requiresProfileSetup = true, // 👈 Force settings screen first
            successMessage = "Welcome Debug User!"
        )
    )
    val uiState = _uiState.asStateFlow()

    // Fake sign-in flow
    fun signInWithGoogle(context: Context): Result<GoogleIdTokenCredential> {
        val fakeCredential = GoogleIdTokenCredential.createFrom(
            Bundle().apply { putString("fake_id_token", "debug_id_12345") }
        )
        return Result.success(fakeCredential)
    }

    fun handleGoogleSignInResult(credential: GoogleIdTokenCredential) {
        _uiState.value = _uiState.value.copy(
            isAuthenticated = true,
            requiresProfileSetup = true,
            successMessage = "Signed in as Debug User"
        )
    }

    fun handleGoogleSignUpResult(credential: GoogleIdTokenCredential) {
        _uiState.value = _uiState.value.copy(
            isAuthenticated = true,
            requiresProfileSetup = true,
            successMessage = "Signed up as Debug User"
        )
    }

    // 👇 Called from SettingsScreen after save
    fun completeProfileSetup() {
        _uiState.value = _uiState.value.copy(
            requiresProfileSetup = false
        )
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun signOut() {
        _uiState.value = _uiState.value.copy(
            isAuthenticated = false,
            requiresProfileSetup = false,
            successMessage = "Signed out"
        )
    }
}
