package com.example.cpen_321.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

data class WaitingRoomState(
    val roomId: String? = null,
    val members: List<String> = emptyList(),
    val completionTime: Int = 600,
    val groupReady: Boolean = false
)

class MatchViewModel: ViewModel(){
    private val _state = MutableStateFlow(WaitingRoomState()) //writable state
    val state: StateFlow<WaitingRoomState> = _state //read-only version that UI can access

    fun connectSocket(userId: String?){
        SocketManager.connect()

        // emit join room request to backend
        val joinData = JSONObject()
        joinData.apply{
            put("userId",userId)
        }
        SocketManager.emit("join_room", joinData)

        // event listener for waiting room events
        SocketManager.on("room_update") { payload ->
            val roomId = payload.optString("roomId", _state.value.roomId ?: "")
            val membersJson = payload.optJSONArray("members")

            val members = mutableListOf<String>()
            if (membersJson != null) {
                for (i in 0 until membersJson.length()) {
                    members.add(membersJson.getString(i))
                }
            }

            viewModelScope.launch {
                _state.value = _state.value.copy(
                    roomId = roomId,
                    members = members
                )
            }
        }

        // event listener for group ready
        SocketManager.on(event = "group_ready"){ payload ->
            viewModelScope.launch {
                _state.value = _state.value.copy(
                    groupReady = true
                )
            }
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
}