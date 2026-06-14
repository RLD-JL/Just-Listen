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

/**
 * Calculate the perfect square artwork size when expanded, capping it
 * so that it leaves enough room at the bottom for controls and spacing (~340dp).
 */
fun expandedArtworkSize(screenWidth: Float, screenHeight: Float): Float {
    // Increase reserved space to 400dp to fully accommodate all control buttons, slider,
    // text column heights, bottom sheet padding, and the top bar offset (totaling ~390dp).
    val reserved = 400f
    return (screenWidth * 0.85f).coerceAtMost(screenHeight - reserved).coerceAtLeast(180f)
}

/**
 * Image width: 49dp when collapsed, centered square when expanded
 */
fun widthSize(fraction: Float, screenWidth: Float, screenHeight: Float): Float {
    return lerp(49f, expandedArtworkSize(screenWidth, screenHeight), fraction)
}

/**
 * Image height: 49dp when collapsed, centered square when expanded
 */
fun heightSize(fraction: Float, screenWidth: Float, screenHeight: Float): Float {
    return lerp(49f, expandedArtworkSize(screenWidth, screenHeight), fraction)
}

/**
 * Horizontal offset:
 * - Collapsed: 8dp from left edge
 * - Expanded: centered horizontally
 */
fun offsetX(fraction: Float, screenWidth: Float, screenHeight: Float): Float {
    val expandedX = (screenWidth - expandedArtworkSize(screenWidth, screenHeight)) / 2f
    return lerp(8f, expandedX, fraction)
}

/**
 * Vertical offset:
 * - Collapsed: 8dp from top (centered in minibar)
 * - Expanded: 72dp from top (8dp under the 64dp top bar)
 */
fun offsetY(fraction: Float, screenWidth: Float, screenHeight: Float): Float {
    val expandedY = 72f
    return lerp(8f, expandedY, fraction)
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