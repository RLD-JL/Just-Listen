package com.example.audius.android.ui.playlistscreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audius.android.ui.playlistscreen.components.graySurface
import com.example.audius.android.ui.test.Album
@Composable
fun SpotifyHomeGridItem(album: Album) {
    val cardColor = if (isSystemInDarkTheme()) graySurface else MaterialTheme.colors.background
    Card(
        elevation = 4.dp,
        backgroundColor = cardColor,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .padding(8.dp)
            .clickable(onClick = {})
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = album.imageId),
                contentDescription = null,
                modifier = Modifier.size(55.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = album.song,
                style = typography.h6.copy(fontSize = 14.sp),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
