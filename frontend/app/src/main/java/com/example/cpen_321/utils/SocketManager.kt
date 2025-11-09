package com.example.cpen_321.utils

import android.util.Log
import com.example.cpen_321.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException

/**
 * Socket Manager for real-time communication
 * Handles Socket.IO connections for waiting rooms and group voting
 */
class SocketManager private constructor() {

    private var socket: Socket? = null
    private var isConnected = false

    companion object {
        private const val TAG = "SocketManager"

        // TODO: Replace with your backend URL
        private const val SOCKET_URL = BuildConfig.IMAGE_BASE_URL
        // For physical device: "http://YOUR_COMPUTER_IP:3000"
        // For production: "https://your-backend-domain.com"

        @Volatile
        private var INSTANCE: SocketManager? = null

        fun getInstance(): SocketManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SocketManager().also { INSTANCE = it }
            }
        }
    }

    /**
     * Connect to socket server with JWT token
     */
    fun connect(token: String) {
        if (isConnected) {
            Log.d(TAG, "Socket already connected")
            return
        }

        try {
            // Debug: Log token info (first 20 chars only for security)
            Log.d(TAG, "Connecting with token: ${token.take(20)}...")
            Log.d(TAG, "Token length: ${token.length}")

            val options = IO.Options().apply {
                auth = mapOf("token" to token)
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
                timeout = 10000
            }

            socket = IO.socket(SOCKET_URL, options)

            // Connection event listeners
            socket?.on(Socket.EVENT_CONNECT, onConnect)
            socket?.on(Socket.EVENT_DISCONNECT, onDisconnect)
            socket?.on(Socket.EVENT_CONNECT_ERROR, onConnectError)

            socket?.connect()

            Log.d(TAG, "Initiating socket connection...")
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Invalid socket URL", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid socket configuration", e)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Socket in invalid state", e)
        }
    }

    /**
     * Disconnect from socket server
     */
    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        isConnected = false
        Log.d(TAG, "Socket disconnected")
    }

    /**
     * Check if socket is connected
     */
    fun isConnected(): Boolean = isConnected

    // ==================== WAITING ROOM EVENTS ====================

    /**
     * Join a room (client → server)
     */
    fun joinRoom(userId: String) {
        val data = JSONObject().apply {
            put("userId", userId)
        }
        emit("join_room", data)
        Log.d(TAG, "Emitted join_room for user: $userId")
    }

    /**
     * Leave a room (client → server)
     */
    fun leaveRoom(userId: String) {
        val data = JSONObject().apply {
            put("userId", userId)
        }
        emit("leave_room", data)
        Log.d(TAG, "Emitted leave_room for user: $userId")
    }

    /**
     * Subscribe to room updates (server → client)
     */
    fun onRoomUpdate(listener: (JSONObject) -> Unit) {
        on("room_update") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                Log.d(TAG, "Received room_update: $data")
                listener(data)
            }
        }
    }

    /**
     * Subscribe to group ready event (server → client)
     */
    fun onGroupReady(listener: (JSONObject) -> Unit) {
        on("group_ready") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                Log.d(TAG, "Received group_ready: $data")
                listener(data)
            }
        }
    }

    /**
     * Subscribe to room expired event (server → client)
     */
    fun onRoomExpired(listener: (JSONObject) -> Unit) {
        on("room_expired") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                Log.d(TAG, "Received room_expired: $data")
                listener(data)
            }
        }
    }

    // ==================== GROUP EVENTS ====================

    /**
     * Subscribe to room/group - FIXED: Send string directly
     */
    fun subscribeToRoom(roomId: String) {
        socket?.emit("subscribe_to_room", roomId)
        Log.d(TAG, "Subscribed to room: $roomId")
    }

    /**
     * Unsubscribe from room/group - FIXED: Send string directly
     */
    fun unsubscribeFromRoom(roomId: String) {
        socket?.emit("unsubscribe_from_room", roomId)
        Log.d(TAG, "Unsubscribed from room: $roomId")
    }

    /**
     * Subscribe to group - FIXED: Send string directly
     */
    fun subscribeToGroup(groupId: String) {
        socket?.emit("subscribe_to_group", groupId)
        Log.d(TAG, "Subscribed to group: $groupId")
    }

    /**
     * Unsubscribe from group - FIXED: Send string directly
     */
    fun unsubscribeFromGroup(groupId: String) {
        socket?.emit("unsubscribe_from_group", groupId)
        Log.d(TAG, "Unsubscribed from group: $groupId")
    }


    /**
     * Subscribe to group with optional userId
     */
    fun subscribeToGroup(groupId: String, userId: String?) {
        if (userId != null) {
            // Send as JSON object with both groupId and userId
            val data = JSONObject().apply {
                put("groupId", groupId)
                put("userId", userId)
            }
            socket?.emit("subscribe_to_group", data)
            Log.d(TAG, "Subscribed to group: $groupId with userId: $userId")
        } else {
            // Fallback: send just groupId as string
            socket?.emit("subscribe_to_group", groupId)
            Log.d(TAG, "Subscribed to group: $groupId (no userId)")
        }
    }

    /**
     * Unsubscribe from group with optional userId
     */
    fun unsubscribeFromGroup(groupId: String, userId: String?) {
        if (userId != null) {
            val data = JSONObject().apply {
                put("groupId", groupId)
                put("userId", userId)
            }
            socket?.emit("unsubscribe_from_group", data)
        } else {
            socket?.emit("unsubscribe_from_group", groupId)
        }
        Log.d(TAG, "Unsubscribed from group: $groupId")
    }

    /**
     * Subscribe to vote updates (server → client)
     */
    fun onVoteUpdate(listener: (JSONObject) -> Unit) {
        on("vote_update") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                Log.d(TAG, "Received vote_update: $data")
                listener(data)
            }
        }
    }

    /**
     * Subscribe to restaurant selected event (server → client)
     */
    fun onRestaurantSelected(listener: (JSONObject) -> Unit) {
        on("restaurant_selected") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                Log.d(TAG, "Received restaurant_selected: $data")
                listener(data)
            }
        }
    }

    /**
     * Subscribe to member joined event (server → client)
     */
    fun onMemberJoined(listener: (JSONObject) -> Unit) {
        on("member_joined") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                Log.d(TAG, "Received member_joined: $data")
                listener(data)
            }
        }
    }

    /**
     * Subscribe to member left event (server → client)
     */
    fun onMemberLeft(listener: (JSONObject) -> Unit) {
        on("member_left") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                Log.d(TAG, "Received member_left: $data")
                listener(data)
            }
        }
    }

    // ==================== HELPER METHODS ====================

    private fun emit(event: String, data: JSONObject) {
        socket?.emit(event, data)
    }

    private fun on(event: String, listener: Emitter.Listener) {
        socket?.on(event, listener)
    }

    /**
     * Remove event listener
     */
    fun off(event: String) {
        socket?.off(event)
        Log.d(TAG, "Removed listener for event: $event")
    }

    /**
     * Remove all event listeners
     */
    fun offAll() {
        socket?.off()
        Log.d(TAG, "Removed all event listeners")
    }

    // ==================== CONNECTION EVENT HANDLERS ====================

    private val onConnect = Emitter.Listener {
        isConnected = true
        Log.d(TAG, "✅ Socket connected successfully")
    }

    private val onDisconnect = Emitter.Listener { args ->
        isConnected = false
        val reason = if (args.isNotEmpty()) args[0].toString() else "unknown"
        Log.d(TAG, "🔌 Socket disconnected. Reason: $reason")
    }

    private val onConnectError = Emitter.Listener { args ->
        isConnected = false
        val error = if (args.isNotEmpty()) args[0].toString() else "unknown error"
        Log.e(TAG, "❌ Socket connection error: $error")

        if (args.isNotEmpty() && args[0] is Exception) {
            val exception = args[0] as Exception
            Log.e(TAG, "Error details: ${exception.message}")
            exception.printStackTrace()
        }
    }
}