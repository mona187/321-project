package com.example.cpen_321.fake

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

object FakeSocketManager {
    private var onRoomUpdate: ((JSONObject) -> Unit)? = null
    private var onGroupReady: ((JSONObject) -> Unit)? = null

    fun connect() {
        println("FakeSocket: connected")
    }

    fun emit(event: String, data: JSONObject) {
        println("FakeSocket emit: $event → $data")
        if (event == "join_room") simulateRoomUpdate()
    }

    fun on(event: String, listener: (JSONObject) -> Unit) {
        when (event) {
            "room_update" -> onRoomUpdate = listener
            "group_ready" -> onGroupReady = listener
        }
    }

    private fun simulateRoomUpdate() {
        // simulate delay to mimic backend update
        GlobalScope.launch {
            delay(1000)
            val fakeMembers = listOf("u1", "u2", "u3")
            val payload = JSONObject().apply {
                put("members", JSONArray(fakeMembers))
            }
            onRoomUpdate?.invoke(payload)

            delay(10000)
            onGroupReady?.invoke(JSONObject())
        }
    }

    fun disconnect() {
        println("FakeSocket: disconnected")
    }
}