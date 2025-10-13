//package com.example.cpen_321.ui.viewmodels
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.cpen_321.data.local.AuthDataStore
//import com.example.cpen_321.data.model.User
//import com.example.cpen_321.data.network.AuthInterceptor
//import com.example.cpen_321.repository.AuthRepository
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//
//data class AuthUiState(
//    val isLoading: Boolean = false,
//    val isLoggedIn: Boolean = false,
//    val user: User? = null,
//    val errorMessage: String? = null,
//    val successMessage: String? = null
//)
//
//class AuthViewModel(
//    private val authRepository: AuthRepository
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(AuthUiState())
//    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
//
//    init {
//        initializeAuth()
//        observeAuthState()
//    }
//
//    private fun initializeAuth() {
//        viewModelScope.launch {
//            authRepository.initializeAuth()
//        }
//    }
//
//    private fun observeAuthState() {
//        viewModelScope.launch {
//            authRepository.isLoggedIn().collect { isLoggedIn ->
//                _uiState.value = _uiState.value.copy(isLoggedIn = isLoggedIn)
//            }
//        }
//    }
//
//    fun login(email: String, password: String) {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
//
//            authRepository.login(email, password)
//                .onSuccess { authResponse ->
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        isLoggedIn = true,
//                        user = authResponse.user,
//                        successMessage = "Login successful"
//                    )
//                }
//                .onFailure { exception ->
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        errorMessage = exception.message ?: "Login failed"
//                    )
//                }
//        }
//    }
//
//    fun register(
//        username: String,
//        email: String,
//        password: String,
//        firstName: String? = null,
//        lastName: String? = null
//    ) {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
//
//            authRepository.register(username, email, password, firstName, lastName)
//                .onSuccess { authResponse ->
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        isLoggedIn = true,
//                        user = authResponse.user,
//                        successMessage = "Registration successful"
//                    )
//                }
//                .onFailure { exception ->
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        errorMessage = exception.message ?: "Registration failed"
//                    )
//                }
//        }
//    }
//
//    fun logout() {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true)
//
//            authRepository.logout()
//                .onSuccess {
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        isLoggedIn = false,
//                        user = null,
//                        successMessage = "Logged out successfully"
//                    )
//                }
//                .onFailure { exception ->
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        errorMessage = exception.message ?: "Logout failed"
//                    )
//                }
//        }
//    }
//
//    fun clearError() {
//        _uiState.value = _uiState.value.copy(errorMessage = null)
//    }
//
//    fun clearSuccessMessage() {
//        _uiState.value = _uiState.value.copy(successMessage = null)
//    }
//}
