package com.example.cpen_321.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen_321.data.model.Restaurant
import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.repository.RestaurantRepository
import com.example.cpen_321.data.repository.UserPreferenceData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for restaurant search and recommendations
 */
@HiltViewModel
class RestaurantViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {

    // Restaurant list
    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    // Selected restaurant details
    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(null)
    val selectedRestaurant: StateFlow<Restaurant?> = _selectedRestaurant.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Search for restaurants near a location
     */
    fun searchRestaurants(
        latitude: Double,
        longitude: Double,
        radius: Int? = null,
        cuisineTypes: List<String>? = null,
        priceLevel: Int? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = restaurantRepository.searchRestaurants(
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                cuisineTypes = cuisineTypes,
                priceLevel = priceLevel
            )) {
                is ApiResult.Success -> {
                    _restaurants.value = result.data
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _restaurants.value = emptyList()
                }
                is ApiResult.Loading -> {
                    // Already handled
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Get restaurant details by ID
     */
    fun getRestaurantDetails(restaurantId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = restaurantRepository.getRestaurantDetails(restaurantId)) {
                is ApiResult.Success -> {
                    _selectedRestaurant.value = result.data
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
                is ApiResult.Loading -> {
                    // Already handled
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Get restaurant recommendations for a group
     */
    fun getGroupRecommendations(
        groupId: String,
        userPreferences: List<UserPreferenceData>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = restaurantRepository.getGroupRecommendations(
                groupId = groupId,
                userPreferences = userPreferences
            )) {
                is ApiResult.Success -> {
                    _restaurants.value = result.data
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _restaurants.value = emptyList()
                }
                is ApiResult.Loading -> {
                    // Already handled
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Select a restaurant
     */
    fun selectRestaurant(restaurant: Restaurant) {
        _selectedRestaurant.value = restaurant
    }

    /**
     * Clear selected restaurant
     */
    fun clearSelectedRestaurant() {
        _selectedRestaurant.value = null
    }

    /**
     * Clear restaurants list
     */
    fun clearRestaurants() {
        _restaurants.value = emptyList()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}