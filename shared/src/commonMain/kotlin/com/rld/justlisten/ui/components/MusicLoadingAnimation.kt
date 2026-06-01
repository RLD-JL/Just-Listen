package com.rld.justlisten.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

/**
 * A premium full-screen music-themed loading screen.
 * Displays a glowing dynamic audio visualizer, radiating sound waves, and drifting floating musical notes.
 */
@Composable
fun MusicLoadingScreen(
    modifier: Modifier = Modifier,
    padding: Dp = 0.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.secondaryContainer

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = padding),
        contentAlignment = Alignment.Center
    ) {
        val transition = rememberInfiniteTransition(label = "music_loader")

        // 1. Concentric pulsing soundwave progress values
        val wave1Progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "wave1"
        )
        
        val wave2Progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, delayMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "wave2"
        )

        // 2. Staggered Equalizer Bar animators (Reversing tween for organic bounce)
        val bar1Height by transition.animateFloat(
            initialValue = 0.25f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(650, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar1"
        )
        val bar2Height by transition.animateFloat(
            initialValue = 0.15f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(850, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar2"
        )
        val bar3Height by transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(750, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar3"
        )
        val bar4Height by transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(950, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar4"
        )
        val bar5Height by transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.75f,
            animationSpec = infiniteRepeatable(
                animation = tween(550, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar5"
        )

        // 3. Floating note particles animations
        val note1Progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "note1"
        )
        val note2Progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3200, delayMillis = 800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "note2"
        )
        val note3Progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2900, delayMillis = 1600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "note3"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val centerX = canvasWidth / 2f
                    val centerY = canvasHeight / 2f

                    // --- DRAW PULSING SOUNDWAVES ---
                    val maxWaveRadius = 100.dp.toPx()
                    val baseWaveRadius = 35.dp.toPx()

                    fun drawWave(progress: Float) {
                        if (progress > 0f) {
                            val currentRadius = baseWaveRadius + (maxWaveRadius - baseWaveRadius) * progress
                            val alpha = (1f - progress) * 0.35f
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(primaryColor.copy(alpha = alpha), Color.Transparent),
                                    center = Offset(centerX, centerY),
                                    radius = currentRadius
                                ),
                                radius = currentRadius,
                                center = Offset(centerX, centerY)
                            )
                            drawCircle(
                                color = primaryColor.copy(alpha = alpha * 0.5f),
                                radius = currentRadius,
                                center = Offset(centerX, centerY),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }

                    drawWave(wave1Progress)
                    drawWave(wave2Progress)

                    // --- DRAW FLOATING MUSIC NOTES ---
                    fun drawFloatingNote(progress: Float, offsetDirection: Float, initialXOffset: Float, type: Int) {
                        if (progress > 0f && progress < 1f) {
                            val fadeAlpha = if (progress < 0.2f) {
                                progress / 0.2f
                            } else {
                                (1f - progress) / 0.8f
                            }
                            
                            val driftY = centerY - 30.dp.toPx() - (progress * 110.dp.toPx())
                            val horizontalSway = sin(progress * 2 * PI.toFloat()) * 25.dp.toPx() * offsetDirection
                            val noteX = centerX + initialXOffset.dp.toPx() + horizontalSway
                            val scale = (0.6f + (progress * 0.6f)) * 4f

                            drawMusicNote(
                                position = Offset(noteX, driftY),
                                scale = scale,
                                alpha = fadeAlpha,
                                color = secondaryColor,
                                type = type
                            )
                        }
                    }

                    // Stagger notes by original horizontal starting values, sway directions, and shapes
                    drawFloatingNote(progress = note1Progress, offsetDirection = -1f, initialXOffset = -25f, type = 0)
                    drawFloatingNote(progress = note2Progress, offsetDirection = 1f, initialXOffset = 20f, type = 1)
                    drawFloatingNote(progress = note3Progress, offsetDirection = -0.5f, initialXOffset = 5f, type = 2)

                    // --- DRAW CENTRAL EQUALIZER BARS ---
                    val barWidth = 6.dp.toPx()
                    val barSpacing = 6.dp.toPx()
                    val totalBarsWidth = (5 * barWidth) + (4 * barSpacing)
                    val startX = centerX - (totalBarsWidth / 2f)
                    val maxBarHeight = 45.dp.toPx()
                    val minBarHeight = 8.dp.toPx()

                    val barHeights = listOf(bar1Height, bar2Height, bar3Height, bar4Height, bar5Height)
                    
                    for (i in 0 until 5) {
                        val currentFraction = barHeights[i]
                        val barHeight = minBarHeight + (maxBarHeight - minBarHeight) * currentFraction
                        val x = startX + i * (barWidth + barSpacing)
                        val y = centerY - (barHeight / 2f)

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(primaryColor, secondaryColor),
                                startY = y,
                                endY = y + barHeight
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tuning your experience...",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * A beautiful, premium, compact loading visualizer designed for buttons, lists, and small loaders.
 * Replaces standard CircularProgressIndicators with a clean, pulsating micro-equalizer.
 */
@Composable
fun MusicLoadingSpinner(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 24.dp
) {
    val transition = rememberInfiniteTransition(label = "music_spinner")

    val bar1Height by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spin_bar1"
    )
    val bar2Height by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spin_bar2"
    )
    val bar3Height by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spin_bar3"
    )

    Canvas(
        modifier = modifier.size(size)
    ) {
        val width = this.size.width
        val height = this.size.height
        
        val barCount = 3
        val spacing = (width * 0.15f).coerceIn(1.dp.toPx(), 4.dp.toPx())
        val barWidth = (width - (spacing * (barCount - 1))) / barCount
        
        val barHeights = listOf(bar1Height, bar2Height, bar3Height)

        for (i in 0 until barCount) {
            val fraction = barHeights[i]
            val maxH = height
            val minH = height * 0.25f
            val currentBarHeight = minH + (maxH - minH) * fraction
            
            val x = i * (barWidth + spacing)
            val y = height - currentBarHeight // Align to bottom (looks extremely elegant)

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, currentBarHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

/**
 * Draws a customized musical note onto a DrawScope at a specified position and scale.
 * Supports different note shapes (type 0: single eighth note, type 1: beamed double note, type 2: simple quarter note).
 */
private fun DrawScope.drawMusicNote(
    position: Offset,
    scale: Float,
    alpha: Float,
    color: Color,
    type: Int
) {
    withTransform({
        translate(position.x, position.y)
        scale(scale, scale, pivot = Offset.Zero)
    }) {
        val noteColor = color.copy(alpha = alpha)
        
        when (type) {
            0 -> {
                // Eighth Note (♪)
                val path = Path().apply {
                    // Ellipse head
                    addOval(Rect(offset = Offset(-3f, 5f), size = Size(8f, 5.5f)))
                    // Vertical Stem
                    moveTo(5f, 7.5f)
                    lineTo(5f, -8f)
                    // Flag
                    cubicTo(5f, -8f, 9f, -6f, 10f, -2f)
                    cubicTo(10.5f, 0.5f, 7.5f, -1f, 5f, -1.5f)
                }
                drawPath(path = path, color = noteColor)
            }
            1 -> {
                // Beamed Double Eighth Note (♫)
                val path = Path().apply {
                    // Left head & stem
                    addOval(Rect(offset = Offset(-3f, 5f), size = Size(7f, 5f)))
                    moveTo(4f, 7f)
                    lineTo(4f, -7f)
                    
                    // Right head & stem
                    addOval(Rect(offset = Offset(8f, 3f), size = Size(7f, 5f)))
                    moveTo(15f, 5f)
                    lineTo(15f, -9f)
                    
                    // Connected Beam
                    moveTo(4f, -7f)
                    lineTo(15f, -9f)
                    lineTo(15f, -6f)
                    lineTo(4f, -4f)
                    close()
                }
                drawPath(path = path, color = noteColor)
            }
            else -> {
                // Quarter Note (♩)
                val path = Path().apply {
                    // Ellipse head
                    addOval(Rect(offset = Offset(-3f, 4f), size = Size(8f, 5.5f)))
                    // Vertical Stem
                    moveTo(5f, 6.5f)
                    lineTo(5f, -9f)
                }
                drawPath(path = path, color = noteColor)
            }
        }
    }
}
