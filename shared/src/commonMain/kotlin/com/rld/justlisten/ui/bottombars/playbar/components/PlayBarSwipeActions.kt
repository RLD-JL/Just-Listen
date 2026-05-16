package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.utils.heightSize
import com.rld.justlisten.ui.utils.offsetX
import com.rld.justlisten.ui.utils.widthSize
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel

@Composable
expect fun PlayBarSwipeActions(
    songIcon: String, highResIcon: String, currentFraction: Float, constraints: BoxWithConstraintsScope, title: String,
    musicPlayer: MusicPlayer, onSkipNextPressed: () -> Unit,
    painterLoaded: (Painter) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    newDominantColor: (Int) -> Unit,
    playBarMinimizedClicked: () -> Unit
)
