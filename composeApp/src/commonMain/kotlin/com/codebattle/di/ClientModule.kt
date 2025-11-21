package com.codebattle.di

import com.codebattle.client.GameClient
import org.koin.dsl.module

val clientModule = module {
    single { GameClient(get()) }
}

