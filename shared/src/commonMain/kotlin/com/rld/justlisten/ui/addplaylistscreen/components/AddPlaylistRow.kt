package com.rld.justlisten.ui.addplaylistscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import justlisten.shared.generated.resources.Res
import justlisten.shared.generated.resources.ic_add_to_playlist_foreground

@Composable
fun AddPlaylistRow(openDialog: MutableState<Boolean>) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = { openDialog.value = true })
    ) {
        Icon(
            painterResource(Res.drawable.ic_add_to_playlist_foreground),
            contentDescription = null,
            modifier = Modifier.height(75.dp)
        )
        Text("New Playlist", modifier = Modifier.fillMaxWidth())
    }
}
