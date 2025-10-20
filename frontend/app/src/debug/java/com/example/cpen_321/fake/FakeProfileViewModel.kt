package com.example.cpen_321.fake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen_321.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class FakeProfileViewModel @Inject constructor(
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()
    private val userRepository = FakeUserRepositoryImpl()

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
