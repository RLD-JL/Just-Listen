package com.rld.justlisten.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.rld.justlisten.ui.theme.*

@Composable
actual fun JustListenTheme(
    darkTheme: Boolean,
    palletColor: String,
    customColors: CustomThemeColors,
    content: @Composable () -> Unit,
) {
    val colorPallet = getColorPallet(palletColor)
    val colors = when (colorPallet) {
        ColorPallet.Green -> if (darkTheme) DarkGreenColorPalette else LightGreenColorPalette
        ColorPallet.Purple -> if (darkTheme) DarkPurpleColorPalette else LightPurpleColorPalette
        ColorPallet.Orange -> if (darkTheme) DarkOrangeColorPalette else LightOrangeColorPalette
        ColorPallet.Blue -> if (darkTheme) DarkBlueColorPalette else LightBlueColorPalette
        ColorPallet.Dark -> if (darkTheme) DarkColorPalette else LightDarkColorPalette
        ColorPallet.Pink -> if (darkTheme) DarkPinkColorPalette else LightPinkColorPalette
        ColorPallet.Custom -> createCustomColorScheme(darkTheme, customColors)
        ColorPallet.Expressive -> if (darkTheme) DarkColorPalette else LightDarkColorPalette
    }

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = Shapes,
        content = content
    )
}

@Composable
actual fun SetSystemBarsColor(
    statusBarColor: Color,
    navigationBarColor: Color,
    darkIcons: Boolean
) {
    // No-op for iOS system status bar, managed natively via Swift/UIKit
}
