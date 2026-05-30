package com.rld.justlisten.ui

import androidx.compose.runtime.Composable
import com.rld.justlisten.ui.theme.ColorPallet
import com.rld.justlisten.ui.utils.getColorPallet
import com.rld.justlisten.ui.theme.JustListenTheme as AndroidJustListenTheme

@Composable
actual fun JustListenTheme(
    darkTheme: Boolean,
    palletColor: String,
    content: @Composable () -> Unit,
) {
    AndroidJustListenTheme(
        darkTheme = darkTheme,
        colorPallet = getColorPallet(palletColor),
        content = content,
    )
}
