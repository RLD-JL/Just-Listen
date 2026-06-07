package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import com.rld.justlisten.ui.utils.lerp

@Composable
fun PlayBarTopSection(
    currentFractionProvider: () -> Float,
    onCollapsedClicked: () -> Unit,
) {
    val isClickable = remember(currentFractionProvider) {
        derivedStateOf { currentFractionProvider() > 0.85f }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .layout { measurable, constraints ->
                val fraction = currentFractionProvider()
                val heightPx = lerp(0f, 64f, fraction).dp.toPx().toInt()
                val placeable = measurable.measure(
                    constraints.copy(
                        minHeight = heightPx,
                        maxHeight = heightPx
                    )
                )
                layout(placeable.width, heightPx) {
                    placeable.place(0, 0)
                }
            }
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ExpandMore,
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer {
                    val fraction = currentFractionProvider()
                    // Only tappable and visible when substantially expanded
                    alpha = ((fraction - 0.85f) / 0.15f).coerceIn(0f, 1f)
                }
                .clickable(enabled = isClickable.value, onClick = onCollapsedClicked),
            contentDescription = "Collapse player",
            tint = Color.White
        )
    }
}