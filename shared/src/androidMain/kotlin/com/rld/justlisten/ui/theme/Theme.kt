package com.rld.justlisten.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme

private val DarkGreenColorPalette = darkColorScheme(
    primary = greenPrimary,
    primaryContainer = greenPrimaryVariant,
    secondary = greenSecondaryPrimary,
    secondaryContainer = greenSecondaryPrimaryVariant,
    background = Color.Black,
    surface = Color.DarkGray,
    error = Color.Red,
    onPrimary = Color.Black,
    onSecondary = greenPrimary,
    onBackground = Color.White,
    onSurface = greenPrimary,
    onError = Color.Black
)

private val DarkPurpleColorPalette = darkColorScheme(
    primary = purple,
    primaryContainer = purpleLight,
    secondary = secondaryPurple,
    secondaryContainer = secondaryPurpleLight,
    background = Color.Black,
    surface = Color.LightGray,
    onPrimary = Color.Black,
    onSecondary = purple,
    onBackground = Color.White,
    onSurface = purple,
    error = Color.Red,
    onError = Color.Black
)


private val LightPurpleColorPalette = lightColorScheme(
    primary = purple,
    primaryContainer = purpleDark,
    secondary = secondaryPurple,
    secondaryContainer = secondaryPurpleDark,
    background = Color.White,
    surface = Color.LightGray,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = Color.Red,
    onError = Color.Black
)


private val DarkBlueColorPalette = darkColorScheme(
    primary = blueCustom,
    primaryContainer = blueCustomLight,
    secondary = secondaryBlue,
    secondaryContainer = secondaryBlueLight,
    background = Color.Black,
    surface = Color.LightGray,
    onPrimary = Color.Black,
    onSecondary = blueCustom,
    onBackground = Color.White,
    onSurface = blueCustom,
    error = Color.Red,
    onError = Color.Black
)

private val DarkOrangeColorPalette = darkColorScheme(
    primary = orange200,
    primaryContainer = orangeLight,
    secondary = secondaryOrange,
    secondaryContainer = secondaryOrangeLight,
    background = Color.Black,
    surface = Color.DarkGray,
    onPrimary = Color.Black,
    onSecondary = orange200,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color.Red,
    onError = Color.Black
)

private val LightOrangeColorPalette = lightColorScheme(
    primary = orange200,
    primaryContainer = orangeDark,
    secondary = secondaryOrange,
    secondaryContainer = secondaryOrangeDark,
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
private val LightGreenColorPalette = lightColorScheme(
    primary = green500,
    primaryContainer = green700,
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



private val DarkColorPalette = darkColorScheme(
    primary = primaryDark000,
    primaryContainer = primaryDarkVariant,
    secondary = secondaryDark,
    secondaryContainer = secondaryDarkVariant,
    background = Color.Black,
    surface = Color.DarkGray,
    error = Color.Red,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black
)

private val LightDarkColorPalette = darkColorScheme(
    primary = primaryLightDark000,
    primaryContainer = primaryLightDarkVariant,
    secondary = secondaryLightDark,
    secondaryContainer = secondaryLightDarkVariant,
    background = Color.LightGray,
    surface = Color.White,
    error = Color.Red,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White
)

private val LightBlueColorPalette = lightColorScheme(
    primary = blueCustom,
    primaryContainer = blueCustomDark,
    secondary = secondaryBlue,
    secondaryContainer = secondaryBlueDark,
    background = Color.White,
    surface = Color.LightGray,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = Color.Red,
    onError = Color.Black
)

private val DarkPinkColorPalette = darkColorScheme(
    primary = pink,
    primaryContainer = pinkLight,
    secondary = secondaryPink,
    secondaryContainer = secondaryPinkLight,
    background = Color.Black,
    surface = Color.DarkGray,
    onPrimary = Color.Black,
    onSecondary = pink,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color.Red,
    onError = Color.Black
)

private val LightPinkColorPalette = lightColorScheme(
    primary = pink,
    primaryContainer = pinkDark,
    secondary = secondaryPink,
    secondaryContainer = secondaryPinkDark,
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
    Purple, Green, Orange, Blue, Dark, Pink, Expressive, Custom
}

fun parseHexColor(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    val cleaned = hex.trim().replace("#", "")
    return try {
        val longVal = cleaned.toLong(16)
        if (cleaned.length == 6) {
            Color(longVal or 0xFF000000)
        } else {
            Color(longVal)
        }
    } catch (_: Exception) {
        fallback
    }
}

private fun createCustomColorScheme(
    darkTheme: Boolean,
    customColors: CustomThemeColors
): ColorScheme {
    val defaultPrimary = if (darkTheme) primaryDark000 else primaryLightDark000
    val defaultSecondary = if (darkTheme) secondaryDark else secondaryLightDark
    val defaultBackground = if (darkTheme) Color.Black else Color.White
    val defaultSurface = if (darkTheme) Color.DarkGray else Color.LightGray

    val primary = parseHexColor(customColors.primary, defaultPrimary)
    val secondary = parseHexColor(customColors.secondary, defaultSecondary)
    val background = parseHexColor(customColors.background, defaultBackground)
    val surface = parseHexColor(customColors.surface, defaultSurface)

    val primaryLuminance = primary.luminance()
    val secondaryLuminance = secondary.luminance()
    val backgroundLuminance = background.luminance()
    val surfaceLuminance = surface.luminance()

    return if (darkTheme) {
        darkColorScheme(
            primary = primary,
            secondary = secondary,
            background = background,
            surface = surface,
            onPrimary = if (primaryLuminance > 0.5f) Color.Black else Color.White,
            onSecondary = if (secondaryLuminance > 0.5f) Color.Black else Color.White,
            onBackground = if (backgroundLuminance > 0.5f) Color.Black else Color.White,
            onSurface = if (surfaceLuminance > 0.5f) Color.Black else Color.White,
        )
    } else {
        lightColorScheme(
            primary = primary,
            secondary = secondary,
            background = background,
            surface = surface,
            onPrimary = if (primaryLuminance > 0.5f) Color.Black else Color.White,
            onSecondary = if (secondaryLuminance > 0.5f) Color.Black else Color.White,
            onBackground = if (backgroundLuminance > 0.5f) Color.Black else Color.White,
            onSurface = if (surfaceLuminance > 0.5f) Color.Black else Color.White,
        )
    }
}

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
