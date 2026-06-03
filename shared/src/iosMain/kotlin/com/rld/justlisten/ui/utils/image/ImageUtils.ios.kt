@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package com.rld.justlisten.ui.utils.image

import coil3.Image
import coil3.toBitmap
import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.UIKit.UIImage

actual suspend fun getImageDominantColor(image: Image): Int? {
    return try {
        val skiaBitmap = image.toBitmap()
        val w = skiaBitmap.width
        val h = skiaBitmap.height
        if (w <= 0 || h <= 0) return null
        
        var sumR = 0L
        var sumG = 0L
        var sumB = 0L
        var count = 0
        
        val stepX = (w / 8).coerceAtLeast(1)
        val stepY = (h / 8).coerceAtLeast(1)
        
        for (x in 0 until w step stepX) {
            for (y in 0 until h step stepY) {
                val color = skiaBitmap.getColor(x, y)
                val r = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val b = color and 0xFF
                sumR += r
                sumG += g
                sumB += b
                count++
            }
        }
        
        if (count == 0) return null
        val avgR = (sumR / count).toInt()
        val avgG = (sumG / count).toInt()
        val avgB = (sumB / count).toInt()
        
        (0xFF shl 24) or (avgR shl 16) or (avgG shl 8) or avgB
    } catch (e: Exception) {
        println("Error extracting dominant color: ${e.message}")
        null
    }
}
