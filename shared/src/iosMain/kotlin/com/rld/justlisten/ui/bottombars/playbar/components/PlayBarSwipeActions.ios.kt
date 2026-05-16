package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.utils.heightSize
import com.rld.justlisten.ui.utils.offsetX
import com.rld.justlisten.ui.utils.widthSize
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel

@Composable
actual fun PlayBarSwipeActions(
    songIcon: String, highResIcon: String, currentFraction: Float, constraints: BoxWithConstraintsScope, title: String,
    musicPlayer: MusicPlayer, onSkipNextPressed: () -> Unit,
    painterLoaded: (Painter) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    newDominantColor: (Int) -> Unit,
    playBarMinimizedClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Placeholder for iOS for now, as Coil isn't yet integrated for iOS in this project's shared module
        Box(
            modifier = Modifier
                .size(
                    width = widthSize(currentFraction, constraints.maxWidth.value).dp,
                    height = heightSize(currentFraction, constraints.maxHeight.value).dp
                )
                .offset(x = offsetX(currentFraction, constraints.maxWidth.value).dp)
                .background(Color.Gray)
        )
        
        PlayBarActionsMinimized(
            currentFraction,
            musicPlayer,
            title,
            onSkipNextPressed,
            onFavoritePressed,
            songIcon,
            playBarMinimizedClicked = playBarMinimizedClicked
        )
    }
}
