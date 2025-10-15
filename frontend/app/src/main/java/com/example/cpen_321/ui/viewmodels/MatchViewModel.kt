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

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val matchRepository: MatchRepository
) : ViewModel() {

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
            val memberIds = payload.optJSONArray("members")?.let { jsonArray ->
                List(jsonArray.length()) { i -> jsonArray.getString(i) }
            } ?: emptyList() //extract memberIds into Kotlin list
            viewModelScope.launch {
                // Fetch user details via Retrofit
                val response = matchRepository.getUserProfilesForRoom(memberIds)
                if (response.isSuccessful) {
                    val profiles = response.body() ?: emptyList()
                    _state.value = _state.value.copy(members = profiles)
                }
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
}