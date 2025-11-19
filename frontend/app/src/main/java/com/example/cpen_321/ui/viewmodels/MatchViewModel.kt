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
import java.text.ParseException
import java.time.format.DateTimeParseException
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


    private fun startTimer(completionTimeMillis: Long) {
        timerJob?.cancel()
        
        val currentTime = System.currentTimeMillis()
        val initialRemaining = completionTimeMillis - currentTime
        
        Log.d(TAG, "Starting timer with completion time: $completionTimeMillis")
        Log.d(TAG, "Current time: $currentTime")
        Log.d(TAG, "Initial remaining: ${initialRemaining / 1000}s")

        if (initialRemaining <= 0) {
            Log.w(TAG, "Timer completion time is in the past, setting to 0")
            _timeRemaining.value = 0
            return
        }

        timerJob = viewModelScope.launch {
            _timeRemaining.value = initialRemaining // Set immediately
            
            while (true) {
                val now = System.currentTimeMillis()
                val remaining = completionTimeMillis - now

                if (remaining <= 0) {
                    _timeRemaining.value = 0
                    Log.d(TAG, "⏰ Timer expired")
                    break
                }

                _timeRemaining.value = remaining

                // Log every 10 seconds
                if (remaining % 10000 < 1000) {
                    Log.d(TAG, "⏱️ Timer: ${remaining / 1000}s remaining")
                }

                delay(1000)
            }
        }
        
        Log.d(TAG, "✅ Timer job started successfully")
    }


    private fun setupSocketListeners() {
        socketManager.onRoomUpdate { data ->
            handleRoomUpdate(data)
        }

        socketManager.onGroupReady { data ->
            handleGroupReady(data)
        }

        socketManager.onRoomExpired { data ->
            handleRoomExpired(data)
        }

        socketManager.onMemberJoined { data ->
            handleMemberJoined(data)
        }

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

                    // Start the client-side countdown timer IMMEDIATELY
                    val completionTime = room.getCompletionTimeMillis()
                    val currentTime = System.currentTimeMillis()
                    Log.d(TAG, "Room completion time string: ${room.completionTime}")
                    Log.d(TAG, "Parsed completion time (ms): $completionTime")
                    Log.d(TAG, "Current time (ms): $currentTime")
                    Log.d(TAG, "Time difference (ms): ${completionTime - currentTime}")
                    
                    if (completionTime > 0 && completionTime > currentTime) {
                        startTimer(completionTime)
                        val secondsRemaining = (completionTime - currentTime) / 1000
                        Log.d(TAG, "✅ Timer started immediately - $secondsRemaining seconds remaining")
                    } else {
                        Log.w(TAG, "⚠️ Initial completion time invalid or in past: $completionTime")
                        Log.w(TAG, "   Will start timer from socket update or room status")
                        // Calculate expected completion time (2 minutes from now) as fallback
                        val fallbackCompletionTime = currentTime + (2 * 60 * 1000) // 2 minutes
                        startTimer(fallbackCompletionTime)
                        Log.d(TAG, "✅ Timer started with fallback time - 120 seconds remaining")
                        
                        // Also try to get room status to get correct completion time
                        viewModelScope.launch {
                            delay(100)
                            getRoomStatus(roomId, updateTimer = true)
                        }
                    }

                    // ✅ FIX: Fetch latest room status to sync member list, but don't restart timer
                    // This ensures we have the most up-to-date member list
                    viewModelScope.launch {
                        delay(500) // Small delay to let backend process
                        getRoomStatus(roomId, updateTimer = false) // Don't update timer, it's already running
                        Log.d(TAG, "Initial room status synced")
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
     * @param updateTimer If true, will update/restart timer if needed. If false, only updates if timer is not running.
     */
    fun getRoomStatus(roomId: String, updateTimer: Boolean = true) {
        viewModelScope.launch {
            when (val result = matchRepository.getRoomStatus(roomId)) {
                is ApiResult.Success -> {
                    val status = result.data

                    // Update timer only if needed
                    val completionTime = status.getCompletionTimeMillis()
                    if (completionTime > 0) {
                        if (updateTimer) {
                            // Only start/update timer if it's not running or if explicitly requested
                            if (_timeRemaining.value == 0L || timerJob == null || timerJob?.isActive == false) {
                                startTimer(completionTime)
                                Log.d(TAG, "Timer started/updated from room status: $completionTime")
                            }
                        } else {
                            // If updateTimer is false, only start if timer is completely stopped
                            if (timerJob == null || timerJob?.isActive == false) {
                                startTimer(completionTime)
                                Log.d(TAG, "Timer started from room status (timer was stopped): $completionTime")
                            }
                        }
                    }

                    // CRITICAL: Don't override groupReady if it's already true
                    // Once the group is ready (from socket), it stays ready
                    if (!_groupReady.value && status.groupReady) {
                        _groupReady.value = true
                    }

                    // Load members if needed
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


    private fun loadRoomMembers(memberIds: List<String>) {
        viewModelScope.launch {
            if (memberIds.isEmpty()) {
                _roomMembers.value = emptyList()
                return@launch
            }

            when (val result = userRepository.getUserProfiles(memberIds)) {
                is ApiResult.Success -> {
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


    private fun handleRoomUpdate(data: JSONObject) {
        viewModelScope.launch {
            val roomId = data.getStringSafe("roomId")
            val members = data.getJSONArraySafe("members")?.toStringList() ?: emptyList()
            val expiresAt = data.getStringSafe("expiresAt")
            val status = data.getStringSafe("status")

            Log.d(TAG, "Room update received - Members: ${members.size}, Status: $status, expiresAt: $expiresAt")

            _currentRoom.value = _currentRoom.value?.copy(members = members)

            _roomMembers.value = emptyList()
            loadRoomMembers(members)

            // Always update timer from socket update (this is the authoritative source)
            val expiresAtMillis = parseIso8601ToMillis(expiresAt)
            if (expiresAtMillis != null) {
                val currentTime = System.currentTimeMillis()
                val remaining = expiresAtMillis - currentTime
                Log.d(TAG, "Socket update - expiresAt: $expiresAtMillis, current: $currentTime, remaining: ${remaining / 1000}s")
                
                if (remaining > 0) {
                    startTimer(expiresAtMillis)
                    Log.d(TAG, "✅ Timer updated from socket - ${remaining / 1000}s remaining")
                } else {
                    Log.w(TAG, "⚠️ Socket expiresAt is in the past, timer already expired")
                    _timeRemaining.value = 0L
                }
            } else {
                Log.w(TAG, "⚠️ Could not parse expiresAt from socket update: $expiresAt")
            }

            if (status == "matched") {
                _groupReady.value = true
            }
        }
    }


    private fun parseIso8601ToMillis(dateString: String?): Long? {
        if (dateString.isNullOrEmpty()) return null

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                java.time.Instant.parse(dateString).toEpochMilli()
            } else {
                val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                format.parse(dateString)?.time
            }
        } catch (e: DateTimeParseException) {
            Log.e(TAG, "Error parsing date with Java 8+ API: $dateString", e)
            null
        } catch (e: ParseException) {
            Log.e(TAG, "Error parsing date with SimpleDateFormat: $dateString", e)
            null
        }
    }


    private fun handleGroupReady(data: JSONObject) {
        viewModelScope.launch {
            val groupId = data.getStringSafe("groupId")
            val ready = data.getBooleanSafe("ready", false)

            Log.d(TAG, "Group ready: $groupId")

            _groupReady.value = ready
            _groupId.value = groupId

            timerJob?.cancel()
        }
    }


    private fun handleRoomExpired(data: JSONObject) {
        viewModelScope.launch {
            Log.d(TAG, "Room expired")

            _roomExpired.value = true
            _errorMessage.value = data.getStringSafe("reason", "Room expired")

            timerJob?.cancel()
        }
    }


    private fun handleMemberJoined(data: JSONObject) {
        viewModelScope.launch {
            val userName = data.getStringSafe("userName")
            val userId = data.getStringSafe("userId")
            Log.d(TAG, "Member joined: $userName")

            _currentRoom.value?.let { room ->
                getRoomStatus(room.roomId)
            }
        }
    }


    private fun handleMemberLeft(data: JSONObject) {
        viewModelScope.launch {
            val userId = data.getStringSafe("userId")
            val userName = data.getStringSafe("userName")

            Log.d(TAG, "Member left: $userName")

            _roomMembers.value = _roomMembers.value.filter { it.userId != userId }.toList()

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