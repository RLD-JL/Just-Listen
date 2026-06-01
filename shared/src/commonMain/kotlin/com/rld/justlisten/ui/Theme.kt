package com.rld.justlisten.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

import com.rld.justlisten.ui.theme.CustomThemeColors

@Composable
expect fun JustListenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palletColor: String = "Dark",
    customColors: CustomThemeColors = CustomThemeColors(),
    content: @Composable () -> Unit,
)

@Composable
expect fun SetSystemBarsColor(
    statusBarColor: Color,
    navigationBarColor: Color,
    darkIcons: Boolean
)

