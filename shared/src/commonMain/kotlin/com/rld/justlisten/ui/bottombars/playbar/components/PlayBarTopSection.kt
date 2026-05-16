package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.rld.justlisten.ui.utils.lerp
import org.jetbrains.compose.resources.painterResource
import justlisten.shared.generated.resources.Res
import justlisten.shared.generated.resources.ic_down_arrow_foreground
import justlisten.shared.generated.resources.ic_more_foreground

@Composable
fun PlayBarTopSection(
    currentFraction: Float, onCollapsedClicked: () -> Unit, onMoreClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(lerp(0f, 50f, currentFraction).dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_down_arrow_foreground),
            modifier = Modifier
                .clickable(onClick = onCollapsedClicked)
                .size(lerp(0f, 50f, currentFraction).dp)
                .graphicsLayer {
                    alpha = if (currentFraction == 1f) 1f else 0f
                },
            contentDescription = null
        )
        Icon(
            painter = painterResource(Res.drawable.ic_more_foreground),
            modifier = Modifier
                .clickable(onClick = onCollapsedClicked)
                .size(lerp(0f, 50f, currentFraction).dp)
                .graphicsLayer {
                    alpha = if (currentFraction == 1f) 1f else 0f
                }
                .clickable(onClick = onMoreClicked),
            contentDescription = null
        )
    }
}
