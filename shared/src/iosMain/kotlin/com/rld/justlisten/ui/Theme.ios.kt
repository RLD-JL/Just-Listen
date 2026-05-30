package com.rld.justlisten.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

@Composable
actual fun JustListenTheme(
    darkTheme: Boolean,
    palletColor: String,
    content: @Composable () -> Unit,
) {
    // iOS: basic dark/light theme support
    // TODO: Add full color palette support matching Android
    MaterialTheme(
        colors = if (darkTheme) darkColors() else lightColors(),
        content = content,
    )
}
