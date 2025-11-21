package com.codebattle.model

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val roomId: String,
    val status: GameStatus,
    val players: List<Player> = emptyList(),
    val taskText: String = "", // Text of the current coding task
    val timeRemainingSeconds: Int = 0
)
