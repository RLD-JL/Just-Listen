package com.rld.justlisten.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        content = content,
    )
}
