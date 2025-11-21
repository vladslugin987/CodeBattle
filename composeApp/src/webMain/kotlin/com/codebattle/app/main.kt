package com.codebattle.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.codebattle.di.clientModule
import com.codebattle.di.initKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin(clientModule)
    ComposeViewport {
        App()
    }
}
