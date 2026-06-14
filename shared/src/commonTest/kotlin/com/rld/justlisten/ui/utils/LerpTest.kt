package com.rld.justlisten.ui.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LerpTest {

    @Test
    fun testBasicLerp() {
        assertEquals(0f, lerp(0f, 10f, 0f))
        assertEquals(5f, lerp(0f, 10f, 0.5f))
        assertEquals(10f, lerp(0f, 10f, 1f))
        assertEquals(2.5f, lerp(0f, 10f, 0.25f))
    }

    @Test
    fun testRangedLerp() {
        // Under startFraction -> return startValue
        assertEquals(0f, lerp(0f, 10f, 0.2f, 0.8f, 0.1f))
        // Over endFraction -> return endValue
        assertEquals(10f, lerp(0f, 10f, 0.2f, 0.8f, 0.9f))
        // Exactly at startFraction
        assertEquals(0f, lerp(0f, 10f, 0.2f, 0.8f, 0.2f))
        // Exactly at endFraction
        assertEquals(10f, lerp(0f, 10f, 0.2f, 0.8f, 0.8f))
        // In the middle (0.5 is exactly midpoint of 0.2..0.8)
        assertEquals(5f, lerp(0f, 10f, 0.2f, 0.8f, 0.5f))
    }

    @Test
    fun testExpandedArtworkSizeCaps() {
        // Under standard mobile size, screen width = 360f, height = 640f
        // expandedArtworkSize = (360 * 0.85).coerceAtMost(640 - 400).coerceAtLeast(180)
        // 360 * 0.85 = 306. 640 - 400 = 240. So it is capped at 240.
        assertEquals(240f, expandedArtworkSize(360f, 640f))

        // Large screen, screen width = 800f, height = 1200f
        // 800 * 0.85 = 680. 1200 - 400 = 800. So it's 680.
        assertEquals(680f, expandedArtworkSize(800f, 1200f))

        // Extremely small height screen, width = 360f, height = 500f
        // 360 * 0.85 = 306. 500 - 400 = 100. Capped by coerceAtLeast(180) -> 180.
        assertEquals(180f, expandedArtworkSize(360f, 500f))
    }

    @Test
    fun testWidthHeightSize() {
        // Collapsed (fraction = 0) should be 49
        assertEquals(49f, widthSize(0f, 360f, 640f))
        assertEquals(49f, heightSize(0f, 360f, 640f))

        // Expanded (fraction = 1) should equal expandedArtworkSize (which is 240 for 360x640)
        assertEquals(240f, widthSize(1f, 360f, 640f))
        assertEquals(240f, heightSize(1f, 360f, 640f))
    }

    @Test
    fun testOffsets() {
        // Collapsed (fraction = 0)
        assertEquals(8f, offsetX(0f, 360f, 640f))
        assertEquals(8f, offsetY(0f, 360f, 640f))

        // Expanded (fraction = 1)
        // offsetX should center the 240f artwork in 360f width: (360 - 240) / 2 = 60
        assertEquals(60f, offsetX(1f, 360f, 640f))
        // offsetY should be 72f
        assertEquals(72f, offsetY(1f, 360f, 640f))
    }
}
