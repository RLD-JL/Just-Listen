package com.rld.justlisten.android.ui.theme.modifiers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode

fun Modifier.horizontalGradientBackground(
    colors: List<Color>
) = gradientBackground(colors) { gradientColors, size ->
    Brush.horizontalGradient(
        colors = gradientColors,
        startX = 0f,
        endX = size.width
    )
}

fun Modifier.verticalGradientBackground(
    colors: List<Color>
) = gradientBackground(colors) { gradientColors, size ->
    Brush.verticalGradient(
        colors = gradientColors,
        startY = 0f,
        endY = size.width
    )
}

fun Modifier.verticalGradientBackgroundColor(
    color: Int
): Modifier {
    val colors = listOf(Color(color), Color(color).copy(alpha = 0.8f))

    return this.gradientBackground(colors) { gradientColors, size ->
        Brush.verticalGradient(
            colors = gradientColors,
            startY = 0f,
            endY = size.width
        )
    }
}

fun Modifier.diagonalGradientTint(
    colors: List<Color>,
    blendMode: BlendMode
) = gradientTint(colors, blendMode) { gradientColors, size ->
    Brush.linearGradient(
        colors = gradientColors,
        start = Offset(
            x = 0f,
            y = 0f
        ),
        end = Offset(
            x = size.width,
            y = size.height
        ),
        tileMode = TileMode.Clamp
    )
}

fun Modifier.gradientBackground(
    colors: List<Color>,
    brushProvider: (List<Color>, Size) -> Brush
): Modifier = composed {
    var size by remember { mutableStateOf(Size.Zero) }
    val gradient = remember(colors, size) { brushProvider(colors, size) }
    drawBehind {
        size = this.size
        drawRect(brush = gradient)
    }
}

fun Modifier.gradientTint(
    colors: List<Color>,
    blendMode: BlendMode,
    brushProvider: (List<Color>, Size) -> Brush
) = composed {
    var size by remember { mutableStateOf(Size.Zero) }
    val gradient = remember(colors, size) { brushProvider(colors, size) }
    drawWithContent {
        drawContent()
        size = this.size
        drawRect(
            brush = gradient,
            blendMode = blendMode
        )
    }
}

