package com.rld.justlisten.ui.playlistdetailscreen.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.rld.justlisten.ui.theme.typography
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailState

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Icon
import com.rld.justlisten.ui.components.CustomPlaceholderIcon

@Composable
fun BoxTopSection(scrollState: MutableState<Float>, playlistDetailState: PlaylistDetailState, playlistPainter: Painter) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val collapsedDistance = with(density) {
            (-scrollState.value).coerceAtLeast(0f).toDp()
        }
        val targetImageSize = (250.dp - collapsedDistance * 0.2f).coerceAtLeast(80.dp)
        val animateImageSize = animateDpAsState(targetImageSize).value

        val hasCoverImage = playlistDetailState.playlistIcon.isNotBlank() || playlistDetailState.songPlaylist.isNotEmpty()
        if (hasCoverImage) {
            Image(
                painter = playlistPainter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(animateImageSize)
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(animateImageSize)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                CustomPlaceholderIcon(
                    modifier = Modifier.fillMaxSize(0.45f)
                )
            }
        }
        Text(
            text = playlistDetailState.playlistName,
            style = typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp),
        )
        Text(
            text = "FOLLOWING",
            style = typography.titleMedium.copy(fontSize = 12.sp, color = Color(0xFFE91E63)),
            modifier = Modifier
                .alpha(0.5f)
                .padding(4.dp)
                .border(
                    border = BorderStroke(1.5.dp, Color(0xFFE91E63)),
                    shape = CircleShape
                )
                .padding(vertical = 6.dp, horizontal = 24.dp)
        )
        Text(
            text = "Created by ${playlistDetailState.playListCreatedBy}",
            maxLines = 1,
            textAlign = TextAlign.Center,
            style = typography.titleSmall,
            modifier = Modifier.padding(4.dp)
        )
    }
}
