package com.rld.justlisten.ui.utils.image

import androidx.compose.ui.graphics.painter.Painter

expect suspend fun getImageDominantColor(painter: Painter): Int?
