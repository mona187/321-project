package com.example.cpen_321.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen_321.data.model.RestaurantRecommendation
import com.example.cpen_321.data.repository.RestaurantRepository
import com.example.cpen_321.data.network.api.UserPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupState(
    val isLoading: Boolean = false,
    val recommendations: List<RestaurantRecommendation> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {

    companion object {
        private const val TAG = "GroupViewModel"
    }

    private val _state = MutableStateFlow(GroupState())
    val state: StateFlow<GroupState> = _state

    fun loadGroupRecommendations(groupId: String, userPreferences: List<UserPreference>) {
        viewModelScope.launch {
            Log.d(TAG, "🔄 Loading group recommendations for groupId: $groupId")
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = restaurantRepository.getGroupRecommendations(groupId, userPreferences)
                Log.d(TAG, "📡 API Response - isSuccessful: ${response.isSuccessful}, code: ${response.code()}")
                if (response.isSuccessful) {
                    val recommendations = response.body() ?: emptyList()
                    Log.d(TAG, "✅ Got ${recommendations.size} recommendations")
                    recommendations.forEachIndexed { index, rec ->
                        Log.d(TAG, "🍽️ Recommendation $index: ${rec.restaurant.name} (score: ${rec.score})")
                    }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        recommendations = recommendations
                    )
                } else {
                    Log.e(TAG, "❌ API Error: ${response.code()}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed with code ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Exception loading recommendations", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error loading recommendations: ${e.message}"
                )
            }
        }
    }
}
