package com.example.audius.android.ui.bottombars.playbar.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.audius.android.R
import com.example.audius.android.ui.utils.lerp

@Composable
fun PlayBarTopSection(
    currentFraction: Float, onCollapsedClicked: () -> Unit, onMoreClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_down_arrow_foreground),
            modifier = Modifier
                .clickable(onClick = onCollapsedClicked)
                .size(lerp(0f, 50f, currentFraction).dp)
                .graphicsLayer {
                    alpha = if (currentFraction == 1f) 1f else 0f
                },
            contentDescription = null
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_more_foreground),
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