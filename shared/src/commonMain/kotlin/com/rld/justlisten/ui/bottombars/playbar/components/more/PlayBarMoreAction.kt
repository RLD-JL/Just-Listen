package com.rld.justlisten.ui.bottombars.playbar.components.more

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PlayBarMoreAction(
    title: String,
    painter: MutableState<Painter?>,
    addToPlaylistClicked: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopSection(title, painter)
        HorizontalDivider(thickness = 2.dp)
        MoreOptions(addToPlaylistClicked)
    }
}

@Composable
fun TopSection(title: String, painter: MutableState<Painter?>) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        painter.value?.let {
            Image(
                painter = it,
                modifier = Modifier
                    .size(65.dp)
                    .padding(8.dp),
                contentDescription = "null"
            )
        }

        Text(
            text = title,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MoreOptions(addToPlaylistClicked: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .size(40.dp)
            .padding(5.dp)
            .clickable { addToPlaylistClicked() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
            contentDescription = "Add to playlist"
        )
        Text(textAlign = TextAlign.Center, text = "Add to Playlist")
    }
    Row(
        Modifier
            .fillMaxWidth()
            .size(40.dp)
            .padding(5.dp)
            .clickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = "Download"
        )
        Text(
            modifier = Modifier,
            text = "Download"
        )
    }
}
