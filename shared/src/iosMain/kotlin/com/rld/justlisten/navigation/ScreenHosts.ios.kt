package com.rld.justlisten.navigation

import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.ui.ExperimentalComposeUiApi
import platform.UIKit.UIViewController
import com.rld.justlisten.ui.JustListenApp

@OptIn(ExperimentalComposeUiApi::class)
fun MainViewController(): UIViewController = ComposeUIViewController(
    configure = {
        parallelRendering = false
    }
) {
    JustListenApp()
}
