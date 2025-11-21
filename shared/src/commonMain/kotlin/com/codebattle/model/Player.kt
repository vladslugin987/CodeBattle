package com.codebattle.model

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val nickname: String,
    val score: Int = 0,
    val codeText: String = "",
    val isReady: Boolean = false,
    val isFinished: Boolean = false,
    val lastRunOutput: String? = null
)

