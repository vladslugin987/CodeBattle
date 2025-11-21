package com.codebattle.model

import kotlinx.serialization.Serializable

@Serializable
enum class GameStatus {
    WAITING,
    COUNTDOWN,
    BATTLE,
    ANALYZING,
    RESULT
}

