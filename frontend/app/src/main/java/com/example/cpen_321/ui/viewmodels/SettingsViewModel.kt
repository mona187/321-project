package com.example.cpen_321.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen_321.data.model.User
import com.example.cpen_321.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository
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
                _user.value = userRepository.getUserSettings()
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
