package com.example.audius.android.ui.theme.utils

import androidx.compose.ui.graphics.Color
import com.example.audius.android.ui.theme.graySurface

object ThemeMode {
    fun spotifySurfaceGradient(isDark: Boolean) =
        if (isDark) listOf(graySurface, Color.Black) else listOf(Color.White, Color.LightGray)
}