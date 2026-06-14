package com.rld.justlisten.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    drawBehind {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFB8B5B5),
                    Color(0xFF8F8B8B),
                    Color(0xFFB8B5B5),
                ),
                start = Offset(translateAnim - 200f, translateAnim - 200f),
                end = Offset(translateAnim, translateAnim)
            )
        )
    }
}

@Composable
fun AnimatedShimmer(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .shimmerEffect()
    )
}

@Composable
fun AnimatedShimmer(
    width: Dp,
    height: Dp,
    cornerRadius: Dp = 8.dp
) {
    AnimatedShimmer(
        modifier = Modifier.size(width = width, height = height),
        cornerRadius = cornerRadius
    )
}