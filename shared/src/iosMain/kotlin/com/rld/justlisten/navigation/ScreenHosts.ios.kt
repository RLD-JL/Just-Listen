package com.rld.justlisten.navigation

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import com.rld.justlisten.ui.JustListenApp

fun MainViewController(): UIViewController = ComposeUIViewController {
    JustListenApp()
}
