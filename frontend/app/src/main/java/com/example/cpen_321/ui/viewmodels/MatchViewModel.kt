package com.example.cpen_321.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen_321.data.model.Room
import com.example.cpen_321.data.model.RoomStatusResponse
import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.network.dto.ApiResult
import com.example.cpen_321.data.repository.MatchRepository
import com.example.cpen_321.data.repository.UserRepository
import com.example.cpen_321.utils.JsonUtils.getBooleanSafe
import com.example.cpen_321.utils.JsonUtils.getJSONArraySafe
import com.example.cpen_321.utils.JsonUtils.getLongSafe
import com.example.cpen_321.utils.JsonUtils.getStringSafe
import com.example.cpen_321.utils.JsonUtils.toStringList
import com.example.cpen_321.utils.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

/**
 * ViewModel for matching/waiting room
 */
@HiltViewModel
class MatchViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository,
    private val socketManager: SocketManager
) : ViewModel() {

    // Current room
    private val _currentRoom = MutableStateFlow<Room?>(null)
    val currentRoom: StateFlow<Room?> = _currentRoom.asStateFlow()

    // Room members (user profiles)
    private val _roomMembers = MutableStateFlow<List<UserProfile>>(emptyList())
    val roomMembers: StateFlow<List<UserProfile>> = _roomMembers.asStateFlow()

    // Time remaining in milliseconds
    private val _timeRemaining = MutableStateFlow<Long>(0L)
    val timeRemaining: StateFlow<Long> = _timeRemaining.asStateFlow()

    // Group ready status
    private val _groupReady = MutableStateFlow(false)
    val groupReady: StateFlow<Boolean> = _groupReady.asStateFlow()

    // Group ID when matched
    private val _groupId = MutableStateFlow<String?>(null)
    val groupId: StateFlow<String?> = _groupId.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Room expired flag
    private val _roomExpired = MutableStateFlow(false)
    val roomExpired: StateFlow<Boolean> = _roomExpired.asStateFlow()

    init {
        setupSocketListeners()
    }

    /**
     * Setup socket listeners for real-time updates
     */
    private fun setupSocketListeners() {
        // Listen for room updates
        socketManager.onRoomUpdate { data ->
            handleRoomUpdate(data)
        }

        // Listen for group ready
        socketManager.onGroupReady { data ->
            handleGroupReady(data)
        }

        // Listen for room expired
        socketManager.onRoomExpired { data ->
            handleRoomExpired(data)
        }

        // Listen for member joined
        socketManager.onMemberJoined { data ->
            handleMemberJoined(data)
        }

        // Listen for member left
        socketManager.onMemberLeft { data ->
            handleMemberLeft(data)
        }
    }

    /**
     * Join matching pool
     */
    fun joinMatching(
        cuisine: List<String>? = null,
        budget: Double? = null,
        radiusKm: Double? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _roomExpired.value = false

            when (val result = matchRepository.joinMatching(cuisine, budget, radiusKm)) {
                is ApiResult.Success -> {
                    val (roomId, room) = result.data
                    _currentRoom.value = room

                    // Subscribe to room updates via socket
                    socketManager.subscribeToRoom(roomId)

                    // Load room members
                    loadRoomMembers(room.members)

                    // Calculate time remaining
                    _timeRemaining.value = room.completionTime - System.currentTimeMillis()
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
     * Leave current room
     */
    fun leaveRoom() {
        viewModelScope.launch {
            val roomId = _currentRoom.value?.roomId ?: return@launch

            _isLoading.value = true

            when (val result = matchRepository.leaveRoom(roomId)) {
                is ApiResult.Success -> {
                    // Unsubscribe from room updates
                    socketManager.unsubscribeFromRoom(roomId)

                    // Clear state
                    _currentRoom.value = null
                    _roomMembers.value = emptyList()
                    _timeRemaining.value = 0L
                    _groupReady.value = false
                    _groupId.value = null
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
     * Get room status
     */
    fun getRoomStatus(roomId: String) {
        viewModelScope.launch {
            when (val result = matchRepository.getRoomStatus(roomId)) {
                is ApiResult.Success -> {
                    val status = result.data
                    _timeRemaining.value = status.completionTime - System.currentTimeMillis()
                    _groupReady.value = status.groupReady

                    // Load members if needed
                    if (_roomMembers.value.isEmpty()) {
                        loadRoomMembers(status.members)
                    }
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
                is ApiResult.Loading -> {
                    // Ignore
                }
            }
        }
    }

    /**
     * Load room members profiles
     */
    private fun loadRoomMembers(memberIds: List<String>) {
        viewModelScope.launch {
            if (memberIds.isEmpty()) return@launch

            when (val result = userRepository.getUserProfiles(memberIds)) {
                is ApiResult.Success -> {
                    _roomMembers.value = result.data
                }
                is ApiResult.Error -> {
                    // Don't show error for member loading
                }
                is ApiResult.Loading -> {
                    // Ignore
                }
            }
        }
    }

    /**
     * Handle room update from socket
     */
    private fun handleRoomUpdate(data: JSONObject) {
        viewModelScope.launch {
            val roomId = data.getStringSafe("roomId")
            val members = data.getJSONArraySafe("members")?.toStringList() ?: emptyList()
            val expiresAt = data.getStringSafe("expiresAt")
            val status = data.getStringSafe("status")

            // Update members
            loadRoomMembers(members)

            // Update time remaining
            try {
                val expiresAtMillis = java.time.Instant.parse(expiresAt).toEpochMilli()
                _timeRemaining.value = expiresAtMillis - System.currentTimeMillis()
            } catch (e: Exception) {
                // Ignore parsing errors
            }

            // Update room status
            if (status == "matched") {
                _groupReady.value = true
            }
        }
    }

    /**
     * Handle group ready from socket
     */
    private fun handleGroupReady(data: JSONObject) {
        viewModelScope.launch {
            val groupId = data.getStringSafe("groupId")
            val ready = data.getBooleanSafe("ready", false)

            _groupReady.value = ready
            _groupId.value = groupId
        }
    }

    /**
     * Handle room expired from socket
     */
    private fun handleRoomExpired(data: JSONObject) {
        viewModelScope.launch {
            _roomExpired.value = true
            _errorMessage.value = data.getStringSafe("reason", "Room expired")
        }
    }

    /**
     * Handle member joined from socket
     */
    private fun handleMemberJoined(data: JSONObject) {
        viewModelScope.launch {
            // Reload room members
            _currentRoom.value?.let { room ->
                getRoomStatus(room.roomId)
            }
        }
    }

    /**
     * Handle member left from socket
     */
    private fun handleMemberLeft(data: JSONObject) {
        viewModelScope.launch {
            val userId = data.getStringSafe("userId")

            // Remove member from list
            _roomMembers.value = _roomMembers.value.filter { it.userId != userId }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clear room expired flag
     */
    fun clearRoomExpired() {
        _roomExpired.value = false
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up socket listeners
        socketManager.off("room_update")
        socketManager.off("group_ready")
        socketManager.off("room_expired")
        socketManager.off("member_joined")
        socketManager.off("member_left")
    }
}