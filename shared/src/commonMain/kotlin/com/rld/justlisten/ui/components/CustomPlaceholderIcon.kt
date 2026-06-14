package com.rld.justlisten.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun CustomPlaceholderIcon(
    modifier: Modifier = Modifier,
    noteColor: Color = MaterialTheme.colorScheme.error,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    dotColor: Color = MaterialTheme.colorScheme.secondary
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Define positions relative to Canvas size (to scale perfectly)
        val noteHead1 = Offset(w * 0.22f, h * 0.76f)
        val noteHead2 = Offset(w * 0.50f, h * 0.58f)
        val headRadius = w * 0.09f

        // Stems
        val stem1Start = Offset(w * 0.30f, h * 0.76f)
        val stem1End = Offset(w * 0.30f, h * 0.32f)
        val stem2Start = Offset(w * 0.58f, h * 0.58f)
        val stem2End = Offset(w * 0.58f, h * 0.14f)
        val stemWidth = w * 0.045f

        // 1. Draw Note Heads
        drawCircle(color = noteColor, radius = headRadius, center = noteHead1)
        drawCircle(color = noteColor, radius = headRadius, center = noteHead2)

        // 2. Draw Stems
        drawLine(color = noteColor, start = stem1Start, end = stem1End, strokeWidth = stemWidth)
        drawLine(color = noteColor, start = stem2Start, end = stem2End, strokeWidth = stemWidth)

        // 3. Draw Beam
        drawLine(
            color = noteColor,
            start = stem1End,
            end = stem2End,
            strokeWidth = stemWidth * 1.8f,
            cap = StrokeCap.Square
        )

        // 4. Draw Horizontal Lines and Circular Endpoints
        val lineEndsX = w * 0.88f
        val lineStartXShort = w * 0.68f
        val lineStartXLong = w * 0.35f
        val lineWidth = w * 0.035f
        val dotRadius = w * 0.045f

        val lineYPositions = listOf(
            h * 0.18f,
            h * 0.33f,
            h * 0.48f,
            h * 0.63f,
            h * 0.78f
        )

        lineYPositions.forEachIndexed { index, y ->
            val startX = if (index == 4) lineStartXLong else lineStartXShort
            drawLine(
                color = lineColor,
                start = Offset(startX, y),
                end = Offset(lineEndsX, y),
                strokeWidth = lineWidth,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = Offset(lineEndsX, y)
            )
        }
    }
}
