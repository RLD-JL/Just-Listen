package com.rld.justlisten.ui.bottombars.playbar.components.addplaylist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun PlatformAddPlaylistButton(
    onClick: () -> Unit,
    modifier: Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val foreground = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = primary.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.24f),
                        primary.copy(alpha = 0.16f),
                        Color.Black.copy(alpha = 0.18f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.12f),
                        primary.copy(alpha = 0.35f)
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                ),
                shape = CircleShape
            )
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = Offset(15f, 10f),
                        radius = 70f
                    )
                )
        )
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "New Playlist",
            tint = foreground,
            modifier = Modifier.size(30.dp)
        )
    }
}
