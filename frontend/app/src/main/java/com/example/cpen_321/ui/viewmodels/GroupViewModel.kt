package com.example.cpen_321.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen_321.data.model.Group
import com.example.cpen_321.data.model.GroupMember
import com.example.cpen_321.data.model.Restaurant
import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.repository.GroupRepository
import com.example.cpen_321.data.repository.UserRepository
import com.example.cpen_321.utils.JsonUtils.getIntSafe
import com.example.cpen_321.utils.JsonUtils.getJSONObjectSafe
import com.example.cpen_321.utils.JsonUtils.getStringSafe
import com.example.cpen_321.utils.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

/**
 * ViewModel for group management and voting
 */
@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val socketManager: SocketManager
) : ViewModel() {

    // Current group
    private val _currentGroup = MutableStateFlow<Group?>(null)
    val currentGroup: StateFlow<Group?> = _currentGroup.asStateFlow()

    // Group members with details
    private val _groupMembers = MutableStateFlow<List<GroupMember>>(emptyList())
    val groupMembers: StateFlow<List<GroupMember>> = _groupMembers.asStateFlow()

    // Current votes (restaurantId -> vote count)
    private val _currentVotes = MutableStateFlow<Map<String, Int>>(emptyMap())
    val currentVotes: StateFlow<Map<String, Int>> = _currentVotes.asStateFlow()

    // Selected restaurant (after voting)
    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(null)
    val selectedRestaurant: StateFlow<Restaurant?> = _selectedRestaurant.asStateFlow()

    // User's vote
    private val _userVote = MutableStateFlow<String?>(null)
    val userVote: StateFlow<String?> = _userVote.asStateFlow()

    // Time remaining in milliseconds
    private val _timeRemaining = MutableStateFlow<Long>(0L)
    val timeRemaining: StateFlow<Long> = _timeRemaining.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Success message
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        setupSocketListeners()
    }

    /**
     * Setup socket listeners for real-time updates
     */
    private fun setupSocketListeners() {
        // Listen for vote updates
        socketManager.onVoteUpdate { data ->
            handleVoteUpdate(data)
        }

        // Listen for restaurant selected
        socketManager.onRestaurantSelected { data ->
            handleRestaurantSelected(data)
        }

        // Listen for member left
        socketManager.onMemberLeft { data ->
            handleMemberLeft(data)
        }
    }

    /**
     * Load group status
     * UPDATED: Properly handles 404 "Not in a group" as a normal state
     */
    fun loadGroupStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = groupRepository.getGroupStatus()) {
                is ApiResult.Success -> {
                    val group = result.data
                    _currentGroup.value = group

                    // Subscribe to group updates via socket
                    group.groupId?.let { groupId ->
                        socketManager.subscribeToGroup(groupId)
                    }

                    // Load group members
                    loadGroupMembers(group.getAllMembers())

                    // Update votes
                    _currentVotes.value = group.restaurantVotes ?: emptyMap()

                    // Update selected restaurant
                    _selectedRestaurant.value = group.restaurant

                    // Calculate time remaining
                    _timeRemaining.value = group.completionTime - System.currentTimeMillis()
                }
                is ApiResult.Error -> {
                    // IMPORTANT: Check if this is a 404 "not in group" error
                    // If so, treat it as a normal state, not an error
                    if (result.code == 404 && result.message.contains("not in a group", ignoreCase = true)) {
                        // User is not in a group - this is a normal state, not an error
                        _currentGroup.value = null
                        clearGroupState()
                        // Don't set error message for this case
                    } else {
                        // This is an actual error
                        _errorMessage.value = result.message
                    }
                }
                is ApiResult.Loading -> {
                    // Already handled
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Vote for a restaurant
     */
    // GroupViewModel.kt - Add detailed logging in voteForRestaurant
    fun voteForRestaurant(restaurantId: String, restaurant: Restaurant) {
        viewModelScope.launch {
            val groupId = _currentGroup.value?.groupId
            Log.d("VoteDebug", "=== VOTE START ===")
            Log.d("VoteDebug", "groupId: $groupId")
            Log.d("VoteDebug", "restaurantId: $restaurantId")
            Log.d("VoteDebug", "restaurant: ${restaurant.name}")

            if (groupId == null) {
                Log.e("VoteDebug", "ERROR: groupId is null!")
                return@launch
            }

            _isLoading.value = true
            _errorMessage.value = null

            when (val result = groupRepository.voteForRestaurant(groupId, restaurantId, restaurant)) {
                is ApiResult.Success -> {
                    Log.d("VoteDebug", "✅ Vote API Success")
                    Log.d("VoteDebug", "Returned votes: ${result.data}")
                    _currentVotes.value = result.data
                    _userVote.value = restaurantId
                    _successMessage.value = "Vote submitted successfully"
                    loadGroupStatus()
                }
                is ApiResult.Error -> {
                    Log.e("VoteDebug", "❌ Vote API Error: ${result.message}")
                    Log.e("VoteDebug", "Error code: ${result.code}")
                    _errorMessage.value = result.message
                }
                is ApiResult.Loading -> {}
            }

            _isLoading.value = false
            Log.d("VoteDebug", "=== VOTE END ===")
        }
    }

    /**
     * Leave current group
     */
    fun leaveGroup(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val groupId = _currentGroup.value?.groupId ?: return@launch

            _isLoading.value = true

            when (val result = groupRepository.leaveGroup(groupId)) {
                is ApiResult.Success -> {
                    // Unsubscribe from group updates
                    socketManager.unsubscribeFromGroup(groupId)

                    // Clear state
                    clearGroupState()

                    _successMessage.value = "Left group successfully"

                    onSuccess()
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
                is ApiResult.Loading -> {
                    // Ignore
                }
            }

            _isLoading.value = false
        }
    }


    /**
     * Subscribe to group socket channel (for external use)
     */
    fun subscribeToGroup(groupId: String) {
        Log.d("SocketDebug", "=== SUBSCRIBING TO GROUP ===")
        Log.d("SocketDebug", "groupId: $groupId")
        Log.d("SocketDebug", "Socket connected: ${socketManager.isConnected()}")
        socketManager.subscribeToGroup(groupId)
        Log.d("SocketDebug", "Subscription command sent")
    }

    /**
     * Unsubscribe from group socket channel (for external use)
     */
    fun unsubscribeFromGroup(groupId: String) {
        socketManager.unsubscribeFromGroup(groupId)
    }


    /**
     * Load group members profiles
     */
    private fun loadGroupMembers(memberIds: List<String>) {
        viewModelScope.launch {
            if (memberIds.isEmpty()) return@launch

            when (val result = userRepository.getUserProfiles(memberIds)) {
                is ApiResult.Success -> {
                    val profiles = result.data
                    val votes = _currentGroup.value?.votes ?: emptyMap()

                    // Convert to GroupMember with vote status
                    val members = profiles.map { profile ->
                        GroupMember(
                            userId = profile.userId,
                            name = profile.name,
                            credibilityScore = 100.0, // Default, update if available
                            phoneNumber = profile.contactNumber,
                            profilePicture = profile.profilePicture,
                            hasVoted = votes.containsKey(profile.userId)
                        )
                    }

                    _groupMembers.value = members
                }
                is ApiResult.Error -> {
                    // Don't show error for member loading failure
                    // Still allow the UI to display the group
                }
                is ApiResult.Loading -> {
                    // Ignore
                }
            }
        }
    }

    /**
     * Handle vote update from socket
     */
    private fun handleVoteUpdate(data: JSONObject) {
        Log.d("SocketDebug", "🔔 VOTE_UPDATE EVENT RECEIVED")
        Log.d("SocketDebug", "Raw data: $data")

        viewModelScope.launch {
            val restaurantId = data.getStringSafe("restaurantId")
            val votes = data.getJSONObjectSafe("votes")

            Log.d("SocketDebug", "restaurantId: $restaurantId")
            Log.d("SocketDebug", "votes JSON: $votes")

            votes?.let { votesJson ->
                val votesMap = mutableMapOf<String, Int>()
                votesJson.keys().forEach { key ->
                    val keyStr = key.toString()
                    val count = votesJson.getIntSafe(keyStr)
                    votesMap[keyStr] = count
                    Log.d("SocketDebug", "Vote: $keyStr -> $count")
                }

                Log.d("SocketDebug", "Setting votes: $votesMap")
                _currentVotes.value = votesMap
                Log.d("SocketDebug", "Votes updated. Current: ${_currentVotes.value}")
            }

            updateMemberVoteStatus()
        }
    }

    /**
     * Handle restaurant selected from socket
     */
    private fun handleRestaurantSelected(data: JSONObject) {
        viewModelScope.launch {
            android.util.Log.d("GroupViewModel", "═══════════════════════════════════")
            android.util.Log.d("GroupViewModel", "🎉 RESTAURANT_SELECTED EVENT RECEIVED!")
            android.util.Log.d("GroupViewModel", "Raw data: $data")

            val restaurantId = data.getStringSafe("restaurantId")
            val restaurantName = data.getStringSafe("restaurantName")
            val votes = data.getJSONObjectSafe("votes")

            android.util.Log.d("GroupViewModel", "restaurantId: $restaurantId")
            android.util.Log.d("GroupViewModel", "restaurantName: $restaurantName")
            android.util.Log.d("GroupViewModel", "votes: $votes")

            // Create Restaurant object
            val restaurant = Restaurant(
                restaurantId = restaurantId,
                name = restaurantName,
                location = "" // Will be filled from full data
            )

            android.util.Log.d("GroupViewModel", "🏪 Created restaurant object: $restaurant")

            android.util.Log.d("GroupViewModel", "⬆️ Setting _selectedRestaurant.value...")
            _selectedRestaurant.value = restaurant
            android.util.Log.d("GroupViewModel", "✅ _selectedRestaurant.value SET!")
            android.util.Log.d("GroupViewModel", "Current value: ${_selectedRestaurant.value}")

            // Update current group's restaurantSelected flag
            _currentGroup.value = _currentGroup.value?.copy(
                restaurantSelected = true,
                restaurant = restaurant
            )

            _successMessage.value = "Restaurant selected: $restaurantName"
            android.util.Log.d("GroupViewModel", "═══════════════════════════════════")
        }
    }

    /**
     * Handle member left from socket
     */
    private fun handleMemberLeft(data: JSONObject) {
        viewModelScope.launch {
            val userId = data.getStringSafe("userId")

            // Remove member from list
            _groupMembers.value = _groupMembers.value.filter { it.userId != userId }

            // Update member count in current group
            _currentGroup.value = _currentGroup.value?.copy(
                numMembers = _groupMembers.value.size
            )
        }
    }

    /**
     * Update member vote status
     */
    private fun updateMemberVoteStatus() {
        val votes = _currentGroup.value?.votes ?: emptyMap()
        _groupMembers.value = _groupMembers.value.map { member ->
            member.copy(hasVoted = votes.containsKey(member.userId))
        }
    }

    /**
     * Clear group state
     */
    private fun clearGroupState() {
        _currentGroup.value = null
        _groupMembers.value = emptyList()
        _currentVotes.value = emptyMap()
        _selectedRestaurant.value = null
        _userVote.value = null
        _timeRemaining.value = 0L
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clear success message
     */
    fun clearSuccess() {
        _successMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up socket listeners
        socketManager.off("vote_update")
        socketManager.off("restaurant_selected")
        socketManager.off("member_left")
    }
}