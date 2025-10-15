import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import org.json.JSONArray

object SocketManager {
    private var socket: Socket? = null
    private const val BASE_URL = "https://your-backend-url.com" // change to your backend

    fun connect() {
        if (socket == null) {
            socket = IO.socket(BASE_URL)
        }
        socket?.connect()
    }

    fun disconnect() {
        socket?.disconnect()
    }

    // take event name and define callback function to run when event received (Ex: "room_update")
    fun on(event: String, listener: (JSONObject) -> Unit) {
        socket?.on(event) { args ->
            if (args.isNotEmpty() && args[0] is JSONObject) {
                listener(args[0] as JSONObject)
            }
        }
    }

    fun emit(event: String, data: JSONObject) {
        socket?.emit(event, data)
    }
}