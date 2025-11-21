package com.codebattle.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.codebattle.di.clientModule
import com.codebattle.di.initKoin

fun main() {
    initKoin(clientModule)
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "CodeBattle",
        ) {
            App()
        }
    }
}
