package com.codebattle.di

import com.codebattle.server.game.RoomManager
import com.codebattle.server.repository.TaskRepository
import org.koin.dsl.module

val serverModule = module {
    single { TaskRepository() }
    single { RoomManager(get()) }
}
