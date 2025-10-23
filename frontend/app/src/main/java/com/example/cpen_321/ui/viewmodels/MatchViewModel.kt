package com.example.cpen_321.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen_321.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.example.cpen_321.data.repository.MatchRepository
import javax.inject.Inject
import com.example.cpen_321.data.model.WaitingRoomState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.threeten.bp.Instant
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import com.example.cpen_321.utils.SocketManager
import com.example.cpen_321.data.network.api.JoinMatchingRequest

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WaitingRoomState()) //writable state
    val state: StateFlow<WaitingRoomState> = _state //read-only version that UI can access

    private var timerJob: Job? = null // Job represents a coroutine, in this case timer

    fun joinMatching(budget: Double, cuisine: List<String>, radiusKm: Double) {
        viewModelScope.launch {
            try {
                val request = JoinMatchingRequest(
                    budget = budget,
                    cuisine = cuisine,
                    radiusKm = radiusKm
                )
                
                val result = matchRepository.joinMatching(request)
                if (result.isSuccessful) {
                    val response = result.body()
                    println("✅ Successfully joined matching pool: $response")
                    
                    // Store roomId for socket communication
                    response?.roomId?.let { roomId ->
                        _state.value = _state.value.copy(roomId = roomId)
                        
                        // Join the room via socket
                        val joinData = JSONObject()
                        joinData.put("roomId", roomId)
                        SocketManager.emit("join_room", joinData)
                    }
                } else {
                    println("❌ Failed to join matching pool: ${result.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("❌ Error joining matching: ${e.message}")
            }
        }
    }

    fun connectSocket(userId: Int?){
        SocketManager.connect()

        // emit join room request to backend
        val joinData = JSONObject()
        joinData.apply{
            put("userId",userId)
        }
        SocketManager.emit("join_room", joinData)

        // event listener for waiting room events
        SocketManager.on("room_update") { payload ->
            val roomId = payload.optString("roomId")
            val expiresAt = payload.optString("expiresAt", null)
            val status = payload.optString("status", "waiting")

            val memberIds = payload.optJSONArray("members")?.let { jsonArray ->
                List(jsonArray.length()) { i -> jsonArray.getInt(i) }
            } ?: emptyList() //extract memberIds into Kotlin list
            viewModelScope.launch {
                // calculate remaining time for countdown
                val remaining = expiresAt?.let {
                    val seconds = (
                            Instant.parse(it).epochSecond - Instant.now().epochSecond).toInt() //Kotlin Instant represents moment in time in UTC
                    seconds.coerceAtLeast(0) //ensure value not negative
                } ?: 0

                // fetch User Profiles from memberIds using repository (API call)
                val response = matchRepository.getUserProfilesForRoom(memberIds)
                if (response.isSuccessful) {
                    val profiles = response.body() ?: emptyList()
                    _state.value = _state.value.copy(members = profiles,
                        expiresAt = expiresAt,
                        timeRemainingSeconds = remaining)
                }
                // start the countdown Job if not already running
                if (timerJob == null) {
                    startCountdown(remaining)
                }
            }

        }


        // event listener for group ready
        SocketManager.on(event = "group_ready"){ payload ->
            viewModelScope.launch {
                val groupId = payload.optString("groupId")
                _state.value = _state.value.copy(
                    groupReady = true,
                    groupId = groupId
                )
                println("🎉 Group ready! GroupId: $groupId")
            }
        }

        // event listener for room expired
        SocketManager.on(event = "room_expired") {

        }

    }

    // user leaves room (client --> server)
    fun leaveRoom(userId: String) {
        val data = JSONObject().apply {
            put("userId", userId)
            _state.value.roomId?.let { put("roomId", it) } //prevent errors if user exits room before join completes
        }
        SocketManager.emit("leave_room", data)
        SocketManager.disconnect()

        // Reset state
        viewModelScope.launch {
            _state.value = WaitingRoomState()
        }
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.disconnect()
    }

    // Timer coroutine using Job
    private fun startCountdown(initialSeconds: Int) {
        timerJob?.cancel() // cancel any existing timer
        timerJob = viewModelScope.launch {
            for (t in initialSeconds downTo 0) {
                _state.value = _state.value.copy(timeRemainingSeconds = t)
                delay(1000L)
            }
            // timer reached zero
            _state.value = _state.value.copy(status = "expired")
        }
    }
}