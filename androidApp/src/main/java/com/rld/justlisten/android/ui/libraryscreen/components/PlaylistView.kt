package com.rld.justlisten.android.ui.libraryscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.android.R

@Composable
fun PlaylistView(onPlayListViewClicked: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 15.dp)
            .clickable(onClick = onPlayListViewClicked),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(id = R.drawable.ic_playlist), contentDescription = null)
        Text(
            modifier = Modifier.padding(start = 5.dp),
            text = "Playlists",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp
        )
    }
}