package com.codebattle.server.game

import com.codebattle.model.GameEvent
import com.codebattle.model.GameState
import com.codebattle.model.GameStatus
import com.codebattle.model.Player
import com.codebattle.server.repository.TaskRepository
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.send
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class RoomManager(private val taskRepository: TaskRepository) {
    private val rooms = ConcurrentHashMap<String, GameRoom>()

    fun createRoom(hostSession: WebSocketSession, hostNickname: String): GameRoom {
        // Generate short 4-char ID (e.g., X7Z9)
        val roomId = generateRoomId()
        val room = GameRoom(roomId, taskRepository)
        rooms[roomId] = room
        room.addPlayer(hostSession, hostNickname)
        return room
    }

    fun joinRoom(roomId: String, session: WebSocketSession, nickname: String): GameRoom? {
        val room = rooms[roomId.uppercase()] ?: return null // Case insensitive join
        room.addPlayer(session, nickname)
        return room
    }

    fun getRoom(roomId: String): GameRoom? = rooms[roomId]
    
    fun removePlayer(session: WebSocketSession) {
        rooms.values.forEach { room ->
            room.removePlayer(session)
            if (room.isEmpty()) {
                rooms.remove(room.roomId)
            }
        }
    }
    
    private fun generateRoomId(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Removed similar looking chars like I, 1, O, 0
        return (1..4)
            .map { chars.random() }
            .joinToString("")
    }
}

class GameRoom(
    val roomId: String,
    private val taskRepository: TaskRepository
) {
    private val _gameState = MutableStateFlow(
        GameState(roomId, GameStatus.WAITING, emptyList())
    )
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val playerSessions = ConcurrentHashMap<String, WebSocketSession>()
    private val gameScope = CoroutineScope(Dispatchers.Default + Job())

    fun addPlayer(session: WebSocketSession, nickname: String) {
        val playerId = UUID.randomUUID().toString()
        val newPlayer = Player(id = playerId, nickname = nickname)
        
        playerSessions[playerId] = session
        
        _gameState.update { currentState ->
            currentState.copy(players = currentState.players + newPlayer)
        }
        
        broadcastState()
    }

    fun removePlayer(session: WebSocketSession) {
        val playerId = playerSessions.entries.find { it.value == session }?.key ?: return
        playerSessions.remove(playerId)
        
        _gameState.update { currentState ->
            currentState.copy(players = currentState.players.filter { it.id != playerId })
        }
        
        broadcastState()
    }

    fun updatePlayerCode(session: WebSocketSession, codeText: String) {
         val playerId = playerSessions.entries.find { it.value == session }?.key ?: return
         
         _gameState.update { currentState ->
             val updatedPlayers = currentState.players.map { player ->
                 if (player.id == playerId) player.copy(codeText = codeText) else player
             }
             currentState.copy(players = updatedPlayers)
         }
         // Optimization: For high frequency updates, we might want to use a dedicated event 
         // instead of broadcasting the full state.
         broadcastState()
    }
    
    fun setPlayerReady(session: WebSocketSession, isReady: Boolean) {
        val playerId = playerSessions.entries.find { it.value == session }?.key ?: return
        
        _gameState.update { currentState ->
             val updatedPlayers = currentState.players.map { player ->
                 if (player.id == playerId) player.copy(isReady = isReady) else player
             }
             currentState.copy(players = updatedPlayers)
        }
        broadcastState()
        checkStartGame()
    }

    private fun checkStartGame() {
        val state = _gameState.value
        if (state.status == GameStatus.WAITING && 
            state.players.isNotEmpty() && 
            state.players.all { it.isReady }) {
            startGame()
        }
    }

    private fun startGame() {
        gameScope.launch {
            // Pick random task
            val task = taskRepository.getRandomTask()
            
            // Countdown
            _gameState.update { it.copy(status = GameStatus.COUNTDOWN, timeRemainingSeconds = 3) }
            broadcastState()
            
            for (i in 3 downTo 1) {
                _gameState.update { it.copy(timeRemainingSeconds = i) }
                broadcastState()
                delay(1000)
            }

            // Start Battle
            _gameState.update { 
                it.copy(
                    status = GameStatus.BATTLE, 
                    taskText = "${task.title}\n\n${task.description}\n\nExample: ${task.inputExample} -> ${task.outputExample}", 
                    timeRemainingSeconds = 60 // TODO: Move duration to config or task
                ) 
            }
            
            // Initialize players code with template
            _gameState.update { state ->
                val playersWithTemplate = state.players.map { it.copy(codeText = task.templateCode) }
                state.copy(players = playersWithTemplate)
            }
            
            broadcastState()
            
            // Game Loop / Timer
            while (_gameState.value.status == GameStatus.BATTLE && _gameState.value.timeRemainingSeconds > 0) {
                delay(1000)
                _gameState.update { it.copy(timeRemainingSeconds = it.timeRemainingSeconds - 1) }
                broadcastState()
            }
            
            // Finish if time runs out
            if (_gameState.value.status == GameStatus.BATTLE) {
                 _gameState.update { it.copy(status = GameStatus.RESULT) }
                 broadcastState()
            }
        }
    }

    private fun broadcastState() {
        val state = _gameState.value
        val event = GameEvent.GameStateUpdate(state)
        val json = Json.encodeToString<GameEvent>(event)
        
        gameScope.launch {
            playerSessions.values.forEach { session ->
                if (session.isActive) {
                    try {
                        session.send(Frame.Text(json))
                    } catch (e: Exception) {
                        // Handle disconnect
                    }
                }
            }
        }
    }
    
    fun isEmpty() = playerSessions.isEmpty()
}
