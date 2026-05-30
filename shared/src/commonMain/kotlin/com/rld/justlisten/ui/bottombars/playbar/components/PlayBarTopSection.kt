package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.rld.justlisten.ui.utils.lerp
import org.jetbrains.compose.resources.painterResource
import justlisten.shared.generated.resources.Res
import justlisten.shared.generated.resources.ic_down_arrow_foreground
import justlisten.shared.generated.resources.ic_more_foreground

@Composable
fun PlayBarTopSection(
    currentFraction: Float,
    onCollapsedClicked: () -> Unit,
    onMoreClicked: () -> Unit,
) {
    // Height grows from 0dp → 64dp as the player expands.
    // Critically: when collapsed this row has 0 height so it doesn't
    // push the image or steal touch events from the minibar.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(lerp(0f, 64f, currentFraction).dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_down_arrow_foreground),
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer {
                    // Only tappable and visible when substantially expanded
                    alpha = ((currentFraction - 0.85f) / 0.15f).coerceIn(0f, 1f)
                }
                .clickable(enabled = currentFraction > 0.85f, onClick = onCollapsedClicked),
            contentDescription = "Collapse player",
            tint = Color.White
        )

        Icon(
            painter = painterResource(Res.drawable.ic_more_foreground),
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer {
                    alpha = ((currentFraction - 0.85f) / 0.15f).coerceIn(0f, 1f)
                }
                .clickable(enabled = currentFraction > 0.85f, onClick = onMoreClicked),
            contentDescription = "More options",
            tint = Color.White
        )
    }
}