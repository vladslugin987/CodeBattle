package com.codebattle.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform