package com.rld.justlisten.android.ui.bottombars.playbar.components.more

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rld.justlisten.android.R
import com.rld.justlisten.android.ui.bottombars.sheetcontent.BottomSheetScreen

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
            .background(MaterialTheme.colors.background)
    ) {
        TopSection(title, painter)
        Divider(thickness = 0.5.dp)
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
            painter = painterResource(id = R.drawable.ic_add_to_playlist_foreground),
            contentDescription = "TODO"
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
            painter = painterResource(id = R.drawable.ic_down_arrow_foreground),
            contentDescription = "TODO"
        )
        Text(
            modifier = Modifier,
            text = "Download"
        )
    }
}