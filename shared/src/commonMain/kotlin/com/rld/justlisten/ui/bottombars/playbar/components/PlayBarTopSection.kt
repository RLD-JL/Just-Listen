package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .height(lerp(0f, 64f, currentFraction).dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_down_arrow_foreground),
            modifier = Modifier
                .clickable(onClick = onCollapsedClicked)
                .size(32.dp)
                .graphicsLayer {
                    alpha = if (currentFraction > 0.9f) 1f else 0f
                },
            contentDescription = null,
            tint = Color.White
        )

        Icon(
            painter = painterResource(Res.drawable.ic_more_foreground),
            modifier = Modifier
                .clickable(onClick = onMoreClicked)
                .size(32.dp)
                .graphicsLayer {
                    alpha = if (currentFraction > 0.9f) 1f else 0f
                },
            contentDescription = null,
            tint = Color.White
        )
    }
}
