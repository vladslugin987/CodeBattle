package com.codebattle.client

import com.codebattle.model.GameEvent
import com.codebattle.model.GameState
import com.codebattle.model.GameStatus
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GameClient(private val client: HttpClient) {
    private val _gameState = MutableStateFlow(GameState("", GameStatus.WAITING, emptyList()))
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()
    
    private var sendEvent: (suspend (GameEvent) -> Unit)? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun connect() {
        scope.launch {
            try {
                _connectionStatus.value = "Connecting..."
                client.webSocket(host = "localhost", port = 8080, path = "/ws") {
                    _connectionStatus.value = "Connected"
                    
                    // Define sender function
                    sendEvent = { event ->
                        val json = Json.encodeToString<GameEvent>(event)
                        send(Frame.Text(json))
                    }

                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            try {
                                val event = Json.decodeFromString<GameEvent>(text)
                                handleEvent(event)
                            } catch (e: Exception) {
                                println("Error decoding event: ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _connectionStatus.value = "Error: ${e.message}"
                e.printStackTrace()
            } finally {
                _connectionStatus.value = "Disconnected"
                sendEvent = null
            }
        }
    }

    private fun handleEvent(event: GameEvent) {
        when (event) {
            is GameEvent.GameStateUpdate -> {
                _gameState.value = event.state
            }
            is GameEvent.Error -> {
                println("Server Error: ${event.message}")
            }
            else -> {}
        }
    }

    fun createRoom(nickname: String) {
        scope.launch {
            sendEvent?.invoke(GameEvent.CreateRoom(nickname))
        }
    }

    fun joinRoom(roomId: String, nickname: String) {
        scope.launch {
            sendEvent?.invoke(GameEvent.JoinRoom(roomId, nickname))
        }
    }

    fun updateCode(code: String) {
        scope.launch {
            sendEvent?.invoke(GameEvent.UpdateCode(code))
        }
    }
    
    fun setReady(isReady: Boolean) {
        scope.launch {
             sendEvent?.invoke(GameEvent.SetReady(isReady))
        }
    }
}

