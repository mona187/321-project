package com.example.cpen_321.fake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen_321.data.model.WaitingRoomState
import com.example.cpen_321.fake.FakeSocketManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlinx.coroutines.Job
import org.threeten.bp.Instant

class FakeMatchViewModel : ViewModel() {
    private val repository = FakeMatchRepository()
    private val _state = MutableStateFlow(WaitingRoomState())
    val state: StateFlow<WaitingRoomState> = _state

    private var timerJob: Job? = null
    init {
        connectSocket("testUser123")
    }

    private fun connectSocket(userId: String?) {
        FakeSocketManager.connect()
        val joinData = JSONObject().apply { put("userId", userId) }
        FakeSocketManager.emit("join_room", joinData)

        FakeSocketManager.on("room_update") { payload ->
            val memberIds = payload.optJSONArray("members")?.let { json ->
                List(json.length()) { i -> json.getString(i) }
            } ?: emptyList()
            val expiresAt = payload.optString("expiresAt", null)
            viewModelScope.launch {
                // calculate remaining time for countdown
                val remaining = expiresAt?.let {
                    val seconds = (
                            Instant.parse(it).epochSecond - Instant.now().epochSecond).toInt() //Kotlin Instant represents moment in time in UTC
                    seconds.coerceAtLeast(0) //ensure value not negative
                } ?: 0

                val response = repository.getUserProfilesForRoom(memberIds)
                if (response.isSuccessful) {
                    val profiles = response.body() ?: emptyList()
                    _state.value = _state.value.copy(members = profiles)
                }

                // start the countdown Job if not already running
                if (timerJob == null) {
                    startCountdown(remaining)
                }
            }
        }

        FakeSocketManager.on("group_ready") {
            _state.value = _state.value.copy(groupReady = true)
        }
    }

    override fun onCleared() {
        FakeSocketManager.disconnect()
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