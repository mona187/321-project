package com.example.cpen_321.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen_321.data.model.User
import com.example.cpen_321.data.repository.UserRepository
import com.example.cpen_321.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    /** Load settings for the current user */
    fun loadUserSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            _isSaved.value = false
            try {
                // Check if user is authenticated
                val isAuthenticated = authRepository.isUserAuthenticated()
                if (isAuthenticated) {
                    // If authenticated, get settings normally
                    _user.value = userRepository.getUserSettings()
                } else {
                    // If not authenticated, try to get cached user
                    val cachedUser = authRepository.getCurrentUser()
                    if (cachedUser != null) {
                        _user.value = cachedUser
                    } else {
                        // No cached user, settings will be empty
                        _user.value = null
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Save settings (either first setup or update) */
    fun saveUserSettings(updatedUser: User, firstTimeSetup: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _isSaved.value = false
            try {
                val savedUser = if (firstTimeSetup) {
                    userRepository.createUserSettings(updatedUser)
                } else {
                    userRepository.updateUserSettings(updatedUser)
                }
                _user.value = savedUser
                _isSaved.value = true
                
                // Cache the user data locally for future use
                // This ensures that when we return to settings, we can load the data
                // even without authentication
                // Note: This assumes AuthRepository has a method to cache user data
                // If not, we'll need to implement local storage
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Helper to update a specific field (called from UI) */
    fun updateField(modify: (User?) -> User?) {
        _user.value = modify(_user.value)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSavedFlag() {
        _isSaved.value = false
    }
}
