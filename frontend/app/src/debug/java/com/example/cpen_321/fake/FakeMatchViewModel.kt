package com.example.cpen_321.fake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen_321.data.model.WaitingRoomState
import com.example.cpen_321.fake.FakeSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class FakeMatchViewModel : ViewModel() {
    private val repository = FakeMatchRepository()
    private val _state = MutableStateFlow(WaitingRoomState())
    val state: StateFlow<WaitingRoomState> = _state

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

            viewModelScope.launch {
                val response = repository.getUserProfilesForRoom(memberIds)
                if (response.isSuccessful) {
                    val profiles = response.body() ?: emptyList()
                    _state.value = _state.value.copy(members = profiles)
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
}