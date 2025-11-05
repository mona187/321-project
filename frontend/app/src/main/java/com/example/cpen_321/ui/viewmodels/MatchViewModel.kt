package com.example.cpen_321.ui.viewmodels

import android.os.Build
import android.util.Log
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    // FIXED: Add timer job for countdown
    private var timerJob: Job? = null

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

    companion object {
        private const val TAG = "MatchViewModel"
    }

    init {
        setupSocketListeners()
    }

    /**
     * FIXED: Start a client-side timer that counts down every second
     */
    private fun startTimer(completionTimeMillis: Long) {
        // Cancel any existing timer
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (true) {
                val currentTime = System.currentTimeMillis()
                val remaining = completionTimeMillis - currentTime

                if (remaining <= 0) {
                    _timeRemaining.value = 0
                    Log.d(TAG, "Timer expired")
                    break
                }

                _timeRemaining.value = remaining

                // Log every 10 seconds to avoid spam
                if (remaining % 10000 < 1000) {
                    Log.d(TAG, "Timer: ${remaining / 1000}s remaining")
                }

                // Wait 1 second before next update
                delay(1000)
            }
        }
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
                    Log.d(TAG, "Subscribed to room: $roomId")

                    // Load room members
                    loadRoomMembers(room.members)

                    // Start the client-side countdown timer
                    startTimer(room.getCompletionTimeMillis())

                    // ✅ ADD THIS: Immediately fetch latest room status
                    // This ensures we have the most up-to-date member list
//                    delay(500)  // Small delay to let backend process
//                    getRoomStatus(roomId)
                    // ✅ FIX: Immediately sync the full room state multiple times
                    viewModelScope.launch {
                        repeat(3) { attempt ->
                            delay(1000L * attempt)
                            getRoomStatus(roomId)
                            Log.d(TAG, "Refreshed room status (attempt $attempt)")
                        }
                    }

                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    Log.e(TAG, "Failed to join matching: ${result.message}")
                }
                is ApiResult.Loading -> {}
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
                    Log.d(TAG, "Unsubscribed from room: $roomId")

                    // FIXED: Stop the timer
                    timerJob?.cancel()

                    // Clear state
                    _currentRoom.value = null
                    _roomMembers.value = emptyList()
                    _timeRemaining.value = 0L
                    _groupReady.value = false
                    _groupId.value = null
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    Log.e(TAG, "Failed to leave room: ${result.message}")
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

                    // FIXED: Don't manually set time, let timer handle it
                    // Just update the timer if needed
                    val completionTime = status.getCompletionTimeMillis()
                    if (_timeRemaining.value == 0L || timerJob == null || timerJob?.isActive == false) {
                        startTimer(completionTime)
                    }

                    // CRITICAL: Don't override groupReady if it's already true
                    // Once the group is ready (from socket), it stays ready
                    if (!_groupReady.value && status.groupReady) {
                        _groupReady.value = true
                    }

                    // Load members if needed
//                    if (_roomMembers.value.isEmpty()) {
//                        loadRoomMembers(status.members)
//                    }
                    if (_roomMembers.value.isEmpty() || _roomMembers.value.size < status.members.size) {
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
            if (memberIds.isEmpty()) {
                _roomMembers.value = emptyList()  // ✅ Clear if empty
                return@launch
            }

            when (val result = userRepository.getUserProfiles(memberIds)) {
                is ApiResult.Success -> {
                    // ✅ CRITICAL: Create new list instance to trigger recomposition
                    _roomMembers.value = result.data.toList()
                    Log.d(TAG, "Loaded ${result.data.size} room members")
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Failed to load members: ${result.message}")
                }
                is ApiResult.Loading -> {}
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

            Log.d(TAG, "Room update - Members: ${members.size}, Status: $status")

            // ✅ CRITICAL: Update room with new member list
            _currentRoom.value = _currentRoom.value?.copy(members = members)

            // ✅ Force clear and reload members to trigger UI update
            _roomMembers.value = emptyList()
            loadRoomMembers(members)

            // Restart timer with updated expiration time
            try {
                val expiresAtMillis = parseIso8601ToMillis(expiresAt)
                if (expiresAtMillis != null) {
                    startTimer(expiresAtMillis)
                }
            } catch (e: java.text.ParseException) {
                Log.e(TAG, "Failed to parse expiration time", e)
            } catch (e: java.time.format.DateTimeParseException) {
                Log.e(TAG, "Failed to parse expiration time", e)
            }

            // Update room status
            if (status == "matched") {
                _groupReady.value = true
            }
        }
    }

    /**
     * Parse ISO 8601 date string to milliseconds
     * Compatible with API 24+
     */
    private fun parseIso8601ToMillis(dateString: String?): Long? {
        if (dateString.isNullOrEmpty()) return null

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Use Java 8 Time API if available (API 26+)
                java.time.Instant.parse(dateString).toEpochMilli()
            } else {
                // Fallback for API 24-25: Use SimpleDateFormat
                val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                format.parse(dateString)?.time
            }
        } catch (e: java.text.ParseException) {
            Log.e(TAG, "Error parsing date: $dateString", e)
            null
        } catch (e: java.time.format.DateTimeParseException) {
            Log.e(TAG, "Error parsing date: $dateString", e)
            null
        }
    }

    /**
     * Handle group ready from socket
     */
    private fun handleGroupReady(data: JSONObject) {
        viewModelScope.launch {
            val groupId = data.getStringSafe("groupId")
            val ready = data.getBooleanSafe("ready", false)

            Log.d(TAG, "Group ready: $groupId")

            _groupReady.value = ready
            _groupId.value = groupId

            // FIXED: Stop timer when group is ready
            timerJob?.cancel()
        }
    }

    /**
     * Handle room expired from socket
     */
    private fun handleRoomExpired(data: JSONObject) {
        viewModelScope.launch {
            Log.d(TAG, "Room expired")

            _roomExpired.value = true
            _errorMessage.value = data.getStringSafe("reason", "Room expired")

            // FIXED: Stop timer when room expires
            timerJob?.cancel()
        }
    }

    /**
     * Handle member joined from socket
     */
    private fun handleMemberJoined(data: JSONObject) {
        viewModelScope.launch {
            val userName = data.getStringSafe("userName")
            val userId = data.getStringSafe("userId")  // ← ADD THIS
            Log.d(TAG, "Member joined: $userName")

            // ✅ BETTER: Directly update members list
            _currentRoom.value?.let { room ->
                // Option 1: Just reload status (current approach)
                getRoomStatus(room.roomId)

                // Option 2: Or manually add member (faster)
                // val updatedRoom = room.copy(members = room.members + userId)
                // _currentRoom.value = updatedRoom
                // loadRoomMembers(updatedRoom.members)
            }
        }
    }

    /**
     * Handle member left from socket
     */
    private fun handleMemberLeft(data: JSONObject) {
        viewModelScope.launch {
            val userId = data.getStringSafe("userId")
            val userName = data.getStringSafe("userName")

            Log.d(TAG, "Member left: $userName")

            // ✅ CRITICAL: Create new list instance
            _roomMembers.value = _roomMembers.value.filter { it.userId != userId }.toList()

            // ✅ Also update the room's member list
            _currentRoom.value = _currentRoom.value?.copy(
                members = _currentRoom.value?.members?.filter { it != userId } ?: emptyList()
            )
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

        // FIXED: Stop timer
        timerJob?.cancel()

        // Clean up socket listeners
        socketManager.off("room_update")
        socketManager.off("group_ready")
        socketManager.off("room_expired")
        socketManager.off("member_joined")
        socketManager.off("member_left")

        Log.d(TAG, "ViewModel cleared")
    }
}