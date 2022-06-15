package com.rld.justlisten.android.ui.playlistdetailscreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rld.justlisten.android.ui.theme.modifiers.horizontalGradientBackground
import com.rld.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailState

@Composable
fun AnimatedToolBar(
    playlistDetailState: PlaylistDetailState,
    scrollState: MutableState<Float>,
    onBackButtonPressed: (Boolean) -> Unit
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
                    ) else listOf(MaterialTheme.colors.background, MaterialTheme.colors.background)
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
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.alpha(0f)
        )
    }
}