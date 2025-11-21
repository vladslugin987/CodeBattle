package com.codebattle.di

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.core.module.Module

fun initKoin(appModule: Module = module {}) {
    startKoin {
        modules(
            appModule,
            sharedModule
        )
    }
}

val sharedModule = module {
    // Common dependencies
}

