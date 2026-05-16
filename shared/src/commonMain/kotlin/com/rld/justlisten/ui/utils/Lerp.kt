package com.rld.justlisten.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp as lerpColor

/**
 * Linearly interpolate between two values
 */
fun lerp(
    startValue: Float,
    endValue: Float,
    fraction: Float
): Float {
    return startValue + fraction * (endValue - startValue)
}

fun widthSize(fraction: Float, screenWidth: Float) : Float {
    return lerp(50f,screenWidth*0.9f,fraction)
}

fun heightSize(fraction: Float, screenWidth: Float) : Float {
    return lerp(50f,screenWidth*0.6f,fraction)
}

fun offsetX(fraction: Float, screenWidth: Float) : Float {
   return lerp(0f, (screenWidth - widthSize(fraction, screenWidth))/2,fraction)
}

fun offsetY(fraction: Float, screenHeight: Float,percent: Float = 0.1f): Float {
   return lerp(0f, screenHeight*percent, fraction)
}

/**
 * Linearly interpolate between two [Float]s when the [fraction] is in a given range.
 */
fun lerp(
    startValue: Float,
    endValue: Float,
    startFraction: Float,
    endFraction: Float,
    fraction: Float
): Float {
    if (fraction < startFraction) return startValue
    if (fraction > endFraction) return endValue

    return lerp(startValue, endValue, (fraction - startFraction) / (endFraction - startFraction))
}

/**
 * Linearly interpolate between two [Color]s when the [fraction] is in a given range.
 */
fun lerp(
    startColor: Color,
    endColor: Color,
    startFraction: Float,
    endFraction: Float,
    fraction: Float
): Color {
    if (fraction < startFraction) return startColor
    if (fraction > endFraction) return endColor

    return lerpColor(
        startColor,
        endColor,
        (fraction - startFraction) / (endFraction - startFraction)
    )
}
