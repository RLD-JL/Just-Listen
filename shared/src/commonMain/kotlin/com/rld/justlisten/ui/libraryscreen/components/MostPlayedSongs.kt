package com.rld.justlisten.ui.libraryscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.viewmodel.screens.library.LibraryState

@Composable
fun MostPlayedSongs(
    libraryState: LibraryState,
    onPlaylistPressed: (String, String, String, String) -> Unit
) {
    val songIcon =
        if (libraryState.mostPlayedSongs.isNotEmpty())
            libraryState.mostPlayedSongs[0].songIconList.songImageURL480px
        else
            ""
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 15.dp)
            .clickable(
                onClick = {
                    onPlaylistPressed(
                        "Most Played",
                        songIcon,
                        "Most Played",
                        "You"
                    )
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.AutoMirrored.Filled.TrendingUp , contentDescription = null)
        Text(
            modifier = Modifier.padding(start = 5.dp),
            text = "Most Played Songs",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp
        )
    }
}
