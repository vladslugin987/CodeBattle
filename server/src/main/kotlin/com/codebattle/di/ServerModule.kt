package com.codebattle.di

import com.codebattle.server.game.RoomManager
import org.koin.dsl.module

val serverModule = module {
    single { RoomManager() }
}
