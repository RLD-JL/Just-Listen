package com.rld.justlisten.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.os.Build

@Composable
fun JustListenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorPallet: ColorPallet = ColorPallet.Dark,
    customColors: CustomThemeColors = CustomThemeColors(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors = when (colorPallet) {
        ColorPallet.Green -> if (darkTheme) DarkGreenColorPalette else LightGreenColorPalette
        ColorPallet.Purple -> if (darkTheme) DarkPurpleColorPalette else LightPurpleColorPalette
        ColorPallet.Orange -> if (darkTheme) DarkOrangeColorPalette else LightOrangeColorPalette
        ColorPallet.Blue -> if (darkTheme) DarkBlueColorPalette else LightBlueColorPalette
        ColorPallet.Dark -> if (darkTheme) DarkColorPalette else LightDarkColorPalette
        ColorPallet.Pink -> if (darkTheme) DarkPinkColorPalette else LightPinkColorPalette
        ColorPallet.Custom -> createCustomColorScheme(darkTheme, customColors)
        ColorPallet.Expressive -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) DarkColorPalette else LightDarkColorPalette
            }
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
