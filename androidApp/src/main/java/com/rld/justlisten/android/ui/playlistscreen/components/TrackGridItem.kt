package com.rld.justlisten.android.ui.playlistscreen.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.rld.justlisten.android.ui.theme.graySurface
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.viewmodel.interfaces.Item

@Composable
fun TrackGridItem(item: Item, onSongPressed: (String, String, String, SongIconList) -> Unit) {
    val cardColor = if (isSystemInDarkTheme()) graySurface else MaterialTheme.colors.background
    Card(
        elevation = 4.dp,
        backgroundColor = cardColor,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .padding(8.dp)
            .clickable(onClick = {onSongPressed(item.id, item.title, item.user, item.songIconList)})
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberImagePainter(item.songIconList.songImageURL150px),
                contentDescription = null,
                modifier = Modifier.size(55.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = item.title,
                style = typography.h6.copy(fontSize = 14.sp),
                maxLines = 3,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
