package com.rld.justlisten.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkGreenColorPalette = darkColors(
    primary = greenPrimary,
    primaryVariant = greenPrimaryVariant,
    secondary = greenSecondaryPrimary,
    secondaryVariant = greenSecondaryPrimaryVariant,
    background = Color.Black,
    surface = Color.DarkGray,
    error = Color.Red,
    onPrimary = Color.Black,
    onSecondary = greenPrimary,
    onBackground = Color.White,
    onSurface = greenPrimary,
    onError = Color.Black
)

private val DarkPurpleColorPalette = darkColors(
    primary = purple,
    primaryVariant = purpleLight,
    secondary = secondaryPurple,
    secondaryVariant = secondaryPurpleLight,
    background = Color.Black,
    surface = Color.LightGray,
    onPrimary = Color.Black,
    onSecondary = purple,
    onBackground = Color.White,
    onSurface = purple,
    error = Color.Red,
    onError = Color.Black
)


private val LightPurpleColorPalette = lightColors(
    primary = purple,
    primaryVariant = purpleDark,
    secondary = secondaryPurple,
    secondaryVariant = secondaryPurpleDark,
    background = Color.White,
    surface = Color.LightGray,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = Color.Red,
    onError = Color.Black
)


private val DarkBlueColorPalette = darkColors(
    primary = blueCustom,
    primaryVariant = blueCustomLight,
    secondary = secondaryBlue,
    secondaryVariant = secondaryBlueLight,
    background = Color.Black,
    surface = Color.LightGray,
    onPrimary = Color.Black,
    onSecondary = blueCustom,
    onBackground = Color.White,
    onSurface = blueCustom,
    error = Color.Red,
    onError = Color.Black
)

private val DarkOrangeColorPalette = darkColors(
    primary = orange200,
    primaryVariant = orangeLight,
    secondary = secondaryOrange,
    secondaryVariant = secondaryOrangeLight,
    background = Color.Black,
    surface = Color.DarkGray,
    onPrimary = Color.Black,
    onSecondary = orange200,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color.Red,
    onError = Color.Black
)

private val LightOrangeColorPalette = lightColors(
    primary = orange200,
    primaryVariant = orangeDark,
    secondary = secondaryOrange,
    secondaryVariant = secondaryOrangeDark,
    background = Color.White,
    surface = Color.LightGray,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.Blue,
    error = Color.Red
)

// Light pallets
private val LightGreenColorPalette = lightColors(
    primary = green500,
    primaryVariant = green700,
    secondary = teal200,
    background = Color.White,
    surface = Color.LightGray,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.Green,
    error = Color.Red
)



private val DarkColorPalette = darkColors(
    primary = primaryDark000,
    primaryVariant = primaryDarkVariant,
    secondary = secondaryDark,
    secondaryVariant = secondaryDarkVariant,
    background = Color.Black,
    surface = Color.DarkGray,
    error = Color.Red,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black
)

private val LightDarkColorPalette = darkColors(
    primary = primaryLightDark000,
    primaryVariant = primaryLightDarkVariant,
    secondary = secondaryLightDark,
    secondaryVariant = secondaryLightDarkVariant,
    background = Color.LightGray,
    surface = Color.White,
    error = Color.Red,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White
)

private val LightBlueColorPalette = lightColors(
    primary = blueCustom,
    primaryVariant = blueCustomDark,
    secondary = secondaryBlue,
    secondaryVariant = secondaryBlueDark,
    background = Color.White,
    surface = Color.LightGray,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = Color.Red,
    onError = Color.Black
)

private val DarkPinkColorPalette = darkColors(
    primary = pink,
    primaryVariant = pinkLight,
    secondary = secondaryPink,
    secondaryVariant = secondaryPinkLight,
    background = Color.Black,
    surface = Color.DarkGray,
    onPrimary = Color.Black,
    onSecondary = pink,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color.Red,
    onError = Color.Black
)

private val LightPinkColorPalette = lightColors(
    primary = pink,
    primaryVariant = pinkDark,
    secondary = secondaryPink,
    secondaryVariant = secondaryPinkDark,
    background = Color.White,
    surface = Color.LightGray,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.Blue,
    error = Color.Red
)

enum class ColorPallet {
    Purple, Green, Orange, Blue, Dark, Pink
}


@Composable
fun JustListenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorPallet: ColorPallet = ColorPallet.Dark,
    content: @Composable () -> Unit,
) {
    val colors = when (colorPallet) {
        ColorPallet.Green -> if (darkTheme) DarkGreenColorPalette else LightGreenColorPalette
        ColorPallet.Purple -> if (darkTheme) DarkPurpleColorPalette else LightPurpleColorPalette
        ColorPallet.Orange -> if (darkTheme) DarkOrangeColorPalette else LightOrangeColorPalette
        ColorPallet.Blue -> if (darkTheme) DarkBlueColorPalette else LightBlueColorPalette
        ColorPallet.Dark -> if (darkTheme) DarkColorPalette else LightDarkColorPalette
        ColorPallet.Pink -> if (darkTheme) DarkPinkColorPalette else LightPinkColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
    /* TODO() CONVERT TO MATERIAL3 THEME
    androidx.compose.material3.MaterialTheme(
        colorScheme = DarkColorPaletteScheme,
        content = content
    )

     */
}
