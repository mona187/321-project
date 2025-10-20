package com.example.cpen_321.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    fun loadUserProfile(userId: Int?) {
        viewModelScope.launch {
            _userProfile.value = if (userId == null) { //get authenticated user profile
                userRepository.getCurrentUserProfile()
            } else {
                userRepository.getUserProfile(userId) //get other user profiles
            }
        }
    }
}
