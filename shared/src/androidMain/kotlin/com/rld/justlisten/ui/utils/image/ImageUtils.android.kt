package com.rld.justlisten.ui.utils.image

import androidx.palette.graphics.Palette
import coil3.Image
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun getImageDominantColor(image: Image): Int? = withContext(Dispatchers.Default) {
    try {
        val originalBitmap = image.toBitmap()
        val bitmap = if (originalBitmap.config == android.graphics.Bitmap.Config.HARDWARE) {
            originalBitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, false)
        } else {
            originalBitmap
        }
        val palette = Palette.from(bitmap).generate()
        palette.dominantSwatch?.rgb ?: palette.vibrantSwatch?.rgb ?: palette.mutedSwatch?.rgb
    } catch (e: Exception) {
        null
    }
}
