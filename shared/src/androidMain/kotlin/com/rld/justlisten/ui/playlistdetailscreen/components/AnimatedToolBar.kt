package com.rld.justlisten.ui.playlistdetailscreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rld.justlisten.ui.theme.modifiers.horizontalGradientBackground
import com.rld.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailState

@Composable
fun AnimatedToolBar(
    playlistDetailState: PlaylistDetailState,
    scrollState: MutableState<Float>,
    onBackButtonPressed: (Boolean) -> Unit,
    onDeletePlaylistClicked: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalGradientBackground(
                if (Dp(-scrollState.value) < 1080.dp)
                    listOf(
                        Color.Transparent,
                        Color.Transparent
                    ) else listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        IconButton(modifier = Modifier.size(48.dp), onClick = { onBackButtonPressed(true) }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
            )
        }
        Text(
            text = playlistDetailState.playlistName,
            modifier = Modifier
                .alpha(((-scrollState.value + 0.010f) / 1000).coerceIn(0f, 1f))
        )
        if (playlistDetailState.playListCreatedBy == "ME") {
            IconButton(onClick = { onDeletePlaylistClicked(playlistDetailState.playlistName) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.Red
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.alpha(0f)
            )
        }
    }
}
