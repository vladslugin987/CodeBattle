package com.codebattle.model

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val inputExample: String,
    val outputExample: String,
    val templateCode: String
)

