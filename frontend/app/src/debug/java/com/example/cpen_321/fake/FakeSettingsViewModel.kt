package com.example.cpen_321.fake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen_321.data.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Fake Settings ViewModel for offline testing without backend.
 * Simulates loading, saving, and simple validation.
 */
class FakeSettingsViewModel : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Simulate fake stored user
    private var localUser: User? = null

    /** Pretend to load existing user settings */
    fun loadUserSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(800) // simulate network delay
            _user.value = localUser ?: User(
                userId = 1,
                name = "Debug User",
                bio = "Just testing the settings flow",
                contactNumber = "1234567890",
                preference = listOf("Italian", "Japanese"),
                profilePicture = "https://cdn-icons-png.flaticon.com/512/1077/1077114.png",
                credibilityScore = null,
                budget = null,
                radiusKm = 15.0,
                status = null,
                roomId = null,
                groupId = null
            )
            _isLoading.value = false
        }
    }

    /** Pretend to save settings — no backend */
    fun saveUserSettings(updatedUser: User, firstTimeSetup: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            delay(1000) // simulate save delay

            localUser = updatedUser
            _user.value = updatedUser
            _isLoading.value = false
            _isSaved.value = true // ✅ triggers navigation in your screen
        }
    }

    /** Clear the saved flag after navigation */
    fun clearSavedFlag() {
        _isSaved.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
