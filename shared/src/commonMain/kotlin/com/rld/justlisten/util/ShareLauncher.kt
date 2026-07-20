package com.rld.justlisten.util

import androidx.compose.runtime.Composable

interface ShareLauncher {
    fun share(text: String, title: String)
}

@Composable
expect fun rememberShareLauncher(): ShareLauncher
