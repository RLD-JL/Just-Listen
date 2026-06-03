package com.rld.justlisten.ui.settingsscreen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import com.rld.justlisten.ui.utils.getStopTimeText

@Composable
fun DialPicker(
    value: Int,
    maxMinutes: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val neutralTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val tickActiveColor = primaryColor.copy(alpha = 0.8f)
    val tickInactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)

    // Calculate dynamic target stop time via platform actuals
    val stopTimeText = remember(value) {
        getStopTimeText(value)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(260.dp)
                .pointerInput(maxMinutes) {
                    detectDragGestures { change, _ ->
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
                        val touchPoint = change.position

                        // Compute drag angle relative to canvas center
                        val angleRad = atan2(touchPoint.y - center.y, touchPoint.x - center.x)
                        var angleDeg = (angleRad * 180.0 / PI).toFloat()
                        
                        // Normalize to [0, 360]
                        if (angleDeg < 0) {
                            angleDeg += 360f
                        }

                        // Arc starts at 135° and sweeps 270° to 405°
                        var relativeAngle = angleDeg - 135f
                        if (relativeAngle < 0) {
                            relativeAngle += 360f
                        }

                        val selectedMinutes = when {
                            relativeAngle in 270f..315f -> maxMinutes
                            relativeAngle > 315f && relativeAngle <= 360f -> 0
                            else -> {
                                val percentage = relativeAngle / 270f
                                (percentage * maxMinutes).roundToInt().coerceIn(0, maxMinutes)
                            }
                        }

                        onValueChange(selectedMinutes)
                        change.consume()
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val strokeWidthPx = 14.dp.toPx()
            val radius = (size.width - strokeWidthPx * 2) / 2f

            // 1. Draw Background Track Arc
            drawArc(
                color = neutralTrackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // 2. Draw Active/Selected Arc Gradient
            val activeSweep = (value.toFloat() / maxMinutes) * 270f
            if (activeSweep > 0f) {
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(primaryColor, secondaryColor),
                        start = Offset(center.x - radius, center.y + radius),
                        end = Offset(center.x + radius, center.y - radius)
                    ),
                    startAngle = 135f,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }

            // 3. Draw Premium Watch Dial Ticks
            val totalTicks = 24
            val tickRadiusStart = radius - strokeWidthPx - 6.dp.toPx()
            val tickRadiusEnd = radius - strokeWidthPx - 14.dp.toPx()

            for (i in 0..totalTicks) {
                val tickPercent = i.toFloat() / totalTicks
                val tickRelativeAngle = tickPercent * 270f
                val tickAbsAngle = tickRelativeAngle + 135f
                val tickRad = tickAbsAngle * PI / 180.0

                val startX = center.x + tickRadiusStart * cos(tickRad).toFloat()
                val startY = center.y + tickRadiusStart * sin(tickRad).toFloat()
                val endX = center.x + tickRadiusEnd * cos(tickRad).toFloat()
                val endY = center.y + tickRadiusEnd * sin(tickRad).toFloat()

                val isActive = (value.toFloat() / maxMinutes) >= tickPercent
                val tickColor = if (isActive) tickActiveColor else tickInactiveColor
                val tickWidth = if (isActive) 2.5.dp.toPx() else 1.5.dp.toPx()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickWidth,
                    cap = StrokeCap.Round
                )
            }

            // 4. Draw Premium Interactive Thumb with Glow Shadow
            val thumbAbsAngle = activeSweep + 135f
            val thumbRad = thumbAbsAngle * PI / 180.0
            val thumbX = center.x + radius * cos(thumbRad).toFloat()
            val thumbY = center.y + radius * sin(thumbRad).toFloat()

            // Outer Glow Circle
            drawCircle(
                color = primaryColor.copy(alpha = 0.25f),
                radius = 18.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )

            // Border Circle
            drawCircle(
                color = primaryColor,
                radius = 10.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )

            // Inner Core Circle (White)
            drawCircle(
                color = Color.White,
                radius = 4.5.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            val hours = value / 60
            val minutesVal = value % 60
            
            val durationText = when {
                hours > 0 && minutesVal > 0 -> "$hours h $minutesVal m"
                hours > 0 -> "$hours h"
                else -> "$minutesVal"
            }

            val labelText = if (hours > 0 && minutesVal == 0) "hours" else if (hours > 0) "duration" else "minutes"

            Text(
                text = durationText,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 48.sp
            )
            
            Text(
                text = labelText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = stopTimeText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
