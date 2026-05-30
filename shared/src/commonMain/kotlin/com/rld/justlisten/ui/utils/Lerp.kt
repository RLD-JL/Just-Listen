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
 * Image width: 49dp when collapsed (fits inside 65dp minibar),
 * 90% of screen width when expanded (matches YT Music large artwork)
 */
fun widthSize(fraction: Float, screenWidth: Float): Float {
    return lerp(49f, screenWidth * 0.90f, fraction)
}

/**
 * Image height: 49dp when collapsed (square thumbnail in minibar),
 * 45% of screen height when expanded (upper half of player)
 */
fun heightSize(fraction: Float, screenHeight: Float): Float {
    return lerp(49f, screenHeight * 0.45f, fraction)
}

/**
 * Horizontal offset:
 * - Collapsed: 8dp from left edge (small gap so it doesn't touch the screen edge)
 * - Expanded: centered horizontally
 */
fun offsetX(fraction: Float, screenWidth: Float): Float {
    val expandedX = (screenWidth - widthSize(fraction, screenWidth)) / 2f
    return lerp(8f, expandedX, fraction)
}

/**
 * Vertical offset:
 * - Collapsed: 8dp from top of the draggable box, which places it centered
 *   in the 65dp minibar  (8dp top gap + 49dp image = 57dp, centred in 65dp)
 * - Expanded: below the 64dp top section (collapse/more icons),
 *   plus a small margin.  64dp top bar + 8dp margin = 72dp ≈ 11% of a
 *   typical 650dp content area, so we use screenHeight * 0.11f.
 */
fun offsetY(fraction: Float, screenHeight: Float, percent: Float = 0.11f): Float {
    return lerp(8f, screenHeight * percent, fraction)
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