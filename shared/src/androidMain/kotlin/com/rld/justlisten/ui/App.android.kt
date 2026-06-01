package com.rld.justlisten.ui

import androidx.compose.runtime.Composable
import com.rld.justlisten.ui.theme.ColorPallet
import com.rld.justlisten.ui.utils.getColorPallet
import com.rld.justlisten.ui.theme.JustListenTheme as AndroidJustListenTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

fun findActivity(context: Context): Activity? {
    var currentContext = context
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

@Composable
actual fun JustListenTheme(
    darkTheme: Boolean,
    palletColor: String,
    customColors: com.rld.justlisten.ui.theme.CustomThemeColors,
    content: @Composable () -> Unit,
) {
    AndroidJustListenTheme(
        darkTheme = darkTheme,
        colorPallet = getColorPallet(palletColor),
        customColors = customColors,
        content = content,
    )
}

@Composable
actual fun SetSystemBarsColor(
    statusBarColor: Color,
    navigationBarColor: Color,
    darkIcons: Boolean
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = findActivity(view.context)
            if (activity != null) {
                val window = activity.window
                window.statusBarColor = statusBarColor.toArgb()
                window.navigationBarColor = navigationBarColor.toArgb()
                
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = darkIcons
                insetsController.isAppearanceLightNavigationBars = darkIcons
            }
        }
    }
}

