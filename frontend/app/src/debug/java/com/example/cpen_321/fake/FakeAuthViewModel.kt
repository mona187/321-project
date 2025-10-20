package com.example.cpen_321.fake

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.example.cpen_321.ui.viewmodels.AuthUiState
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A fully compatible fake AuthViewModel for offline/testing.
 * Mirrors the real AuthViewModel API used across your screens.
 */
class FakeAuthViewModel : ViewModel() {

    // Fake UI state (pretend user is logged in)
    private val _uiState = MutableStateFlow(
        AuthUiState(
            isAuthenticated = true,
            isSigningIn = false,
            isSigningUp = false,
            successMessage = "Welcome Debug User!",
            errorMessage = null
        )
    )
    val uiState = _uiState.asStateFlow()

    // ----------------------------------------------------------
    //  Fake Auth methods matching real AuthViewModel
    // ----------------------------------------------------------

    fun signInWithGoogle(context: Context): Result<GoogleIdTokenCredential> {
        val fakeCredential = GoogleIdTokenCredential.createFrom(
            Bundle().apply { putString("fake_id_token", "debug_id_12345") }
        )
        return Result.success(fakeCredential)
    }

    fun handleGoogleSignInResult(credential: GoogleIdTokenCredential) {
        _uiState.value = _uiState.value.copy(
            isAuthenticated = true,
            successMessage = "Signed in as Debug User"
        )
    }

    fun handleGoogleSignUpResult(credential: GoogleIdTokenCredential) {
        _uiState.value = _uiState.value.copy(
            isAuthenticated = true,
            successMessage = "Signed up as Debug User"
        )
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // ----------------------------------------------------------
    //  Add signOut() to support Home/Profile screen actions
    // ----------------------------------------------------------

    fun signOut() {
        _uiState.value = _uiState.value.copy(
            isAuthenticated = false,
            successMessage = "Signed out",
            errorMessage = null,
            isSigningIn = false,
            isSigningUp = false
        )
    }
}
