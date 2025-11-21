package com.codebattle.model

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val roomId: String,
    val status: String,
    val players: List<String>
)

