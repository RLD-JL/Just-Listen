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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

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

    // Calculate dynamic target stop time
    val stopTimeText = remember(value) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, value)
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        "Stops at ${sdf.format(calendar.time)}"
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Core Interactive Canvas for drawing Dial Arc, Ticks, and Thumb
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
                        var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
                        
                        // Normalize to [0, 360]
                        if (angleDeg < 0) {
                            angleDeg += 360f
                        }

                        // Arc starts at 135° (bottom-left) and sweeps 270° to 405° (bottom-right, same as 45°)
                        // Calculate relative angle from start (135°)
                        var relativeAngle = angleDeg - 135f
                        if (relativeAngle < 0) {
                            relativeAngle += 360f
                        }

                        // Snapping boundary in the bottom gap (45° to 135°, relative 270° to 360°)
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

            // 1. Draw Background Track Arc (135° to 405° -> 270° sweep)
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
            val totalTicks = 24 // Draw a tick mark every 15 minutes of normalized range
            val tickRadiusStart = radius - strokeWidthPx - 6.dp.toPx()
            val tickRadiusEnd = radius - strokeWidthPx - 14.dp.toPx()

            for (i in 0..totalTicks) {
                val tickPercent = i.toFloat() / totalTicks
                val tickRelativeAngle = tickPercent * 270f
                val tickAbsAngle = tickRelativeAngle + 135f
                val tickRad = Math.toRadians(tickAbsAngle.toDouble())

                val startX = center.x + tickRadiusStart * cos(tickRad).toFloat()
                val startY = center.y + tickRadiusStart * sin(tickRad).toFloat()
                val endX = center.x + tickRadiusEnd * cos(tickRad).toFloat()
                val endY = center.y + tickRadiusEnd * sin(tickRad).toFloat()

                // Tick is active if the current value progress is past it
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
            val thumbRad = Math.toRadians(thumbAbsAngle.toDouble())
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

        // Center Content (Minutes indicator + Stops at Target Time)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            val hours = value / 60
            val minutes = value % 60
            
            val durationText = when {
                hours > 0 && minutes > 0 -> "$hours h $minutes m"
                hours > 0 -> "$hours h"
                else -> "$minutes"
            }

            val labelText = if (hours > 0 && minutes == 0) "hours" else if (hours > 0) "duration" else "minutes"

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
