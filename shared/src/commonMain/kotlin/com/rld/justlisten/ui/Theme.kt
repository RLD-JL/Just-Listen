package com.rld.justlisten.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
expect fun JustListenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palletColor: String = "Dark",
    content: @Composable () -> Unit,
)
