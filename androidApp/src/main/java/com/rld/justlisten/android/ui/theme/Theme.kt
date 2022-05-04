package com.rld.justlisten.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.shapes
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle


private val DarkGreenColorPalette = darkColors(
    primary = green200,
    primaryVariant = green700,
    secondary = teal200,
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color.Red,
)

private val DarkPurpleColorPalette = darkColors(
    primary = purple200,
    primaryVariant = purple700,
    secondary = teal200,
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color.Red,
)

private val DarkBlueColorPalette = darkColors(
    primary = blue200,
    primaryVariant = blue700,
    secondary = teal200,
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color.Red,
)

private val DarkOrangeColorPalette = darkColors(
    primary = orange200,
    primaryVariant = orange700,
    secondary = teal200,
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color.Red,
)

// Light pallets
private val LightGreenColorPalette = lightColors(
    primary = green500,
    primaryVariant = green700,
    secondary = teal200,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

private val LightPurpleColorPalette = lightColors(
    primary = purple,
    primaryVariant = purple700,
    secondary = teal200,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

private val LightBlueColorPalette = lightColors(
    primary = blue500,
    primaryVariant = blue700,
    secondary = teal200,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

private val LightOrangeColorPalette = lightColors(
    primary = orange500,
    primaryVariant = orange700,
    secondary = teal200,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)


private val DarkColorPalette = darkColors(
    primary = Color.White,
    primaryVariant = primaryGray,
    secondary = secondDark,
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color.Red
)

private val DarkColorPalettev2 = darkColors(
    primary = primaryDark000,
    primaryVariant = primaryDarkVariant,
    secondary = secondaryDark,
    secondaryVariant = secondaryDarkVariant,
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    error = Color(0xFFCF6679),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black
)

private val LightDarkColorPalettev2 = darkColors(
    primary = primaryLightDark000,
    onPrimary = Color.White,
    primaryVariant = primaryLightDarkVariant,
    secondary = secondaryLightDark,
    onSecondary = Color.Black,
    secondaryVariant = secondaryLightDarkVariant,
    background = backgroundLightDarkVariant,
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    error = Color.Red,
    onError = Color.White
)


private val LightColorPalette = lightColors(
    primary = Color.Black,
    primaryVariant = primaryGray,
    secondary = Color.DarkGray,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.Blue,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = Color.Red
)

enum class ColorPallet {
    PURPLE, GREEN, ORANGE, BLUE, DARK
}


@Composable
fun JustListenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorPallet: ColorPallet = ColorPallet.DARK,
    content: @Composable () -> Unit,
) {
    val colors = when (colorPallet) {
        ColorPallet.GREEN -> if (darkTheme) DarkGreenColorPalette else LightGreenColorPalette
        ColorPallet.PURPLE -> if (darkTheme) DarkPurpleColorPalette else LightPurpleColorPalette
        ColorPallet.ORANGE -> if (darkTheme) DarkOrangeColorPalette else LightOrangeColorPalette
        ColorPallet.BLUE -> if (darkTheme) DarkBlueColorPalette else LightBlueColorPalette
        ColorPallet.DARK -> if (darkTheme) DarkColorPalette else LightDarkColorPalettev2
    }

    MaterialTheme(
        colors = colors,
        typography = typography,
        shapes = shapes,
        content = {
            ProvideTextStyle(
                value = TextStyle(color = Color.White),
                content = content
            )
        }
    )
    /* TODO() CONVERT TO MATERIAL3 THEME
    androidx.compose.material3.MaterialTheme(
        colorScheme = DarkColorPaletteScheme,
        content = content
    )

     */
}
