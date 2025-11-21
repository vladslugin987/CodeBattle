package com.codebattle.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class GameEvent {

    // Client -> Server
    @Serializable
    @SerialName("create_room")
    data class CreateRoom(val nickname: String) : GameEvent()

    @Serializable
    @SerialName("join_room")
    data class JoinRoom(val roomId: String, val nickname: String) : GameEvent()

    @Serializable
    @SerialName("update_code")
    data class UpdateCode(val codeText: String) : GameEvent()

    @Serializable
    @SerialName("submit_solution")
    data class SubmitSolution(val codeText: String) : GameEvent()
    
    @Serializable
    @SerialName("set_ready")
    data class SetReady(val isReady: Boolean) : GameEvent()

    @Serializable
    @SerialName("leave_room")
    object LeaveRoom : GameEvent()

    @Serializable
    @SerialName("run_code")
    data class RunCode(val codeText: String) : GameEvent()

    // Server -> Client
    @Serializable
    @SerialName("game_state_update")
    data class GameStateUpdate(val state: GameState) : GameEvent()

    @Serializable
    @SerialName("game_result")
    data class GameResult(val winnerId: String?, val scores: Map<String, Int>) : GameEvent()
    
    @Serializable
    @SerialName("run_result")
    data class RunResult(val output: String) : GameEvent()

    @Serializable
    @SerialName("error")
    data class Error(val message: String) : GameEvent()
}

