package com.rld.justlisten.ui.bottombars.playbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.bottombars.playbar.components.PlayerBottomBar
import com.rld.justlisten.ui.bottombars.sheets.BottomSheetScreen
import com.rld.justlisten.ui.bottombars.sheets.SheetLayout
import kotlinx.coroutines.delay

@ExperimentalMaterialApi
@Composable
fun PlayerBarSheetContent(
    bottomPadding: Dp,
    currentFraction: Float,
    isExtended: Boolean,
    onSkipNextPressed: () -> Unit,
    musicPlayer: MusicPlayer,
    onCollapsedClicked: () -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    addPlaylistList: List<AddPlaylist>,
    onAddPlaylistClicked: (String, String?) -> Unit,
    getLatestPlaylist: () -> Unit,
    clickedToAddSongToPlaylist: (String, String?, List<String>) -> Unit,
    newDominantColor: (Int) -> Unit,
    playBarMinimizedClicked: () -> Unit,
) {
    val playbackState by musicPlayer.playbackState.collectAsState()

    // 1. Create mutable states instead of derived states
    var songIcon by remember { mutableStateOf("") }
    var artworkUrl by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    // 2. Key the LaunchedEffect to the ID of the current media
    val currentMediaId = playbackState.currentMedia?.id

    LaunchedEffect(currentMediaId) {
        // 3. Add a small delay (debounce).
        // If the ID changes again before 200ms, this coroutine cancels and restarts.
        delay(200)

        // 4. Update the UI state only after the player has settled
        songIcon = playbackState.currentMedia?.lowResArtworkUrl
            ?: playbackState.currentMedia?.artworkUrl
                    ?: ""
        artworkUrl = playbackState.currentMedia?.artworkUrl ?: ""
        title = playbackState.currentMedia?.title ?: ""
    }

    val mutablePainter = remember { mutableStateOf<Painter?>(null) }

    // Track which secondary sheet is open (More / AddPlaylist)
    var currentBottomSheet: BottomSheetScreen? by remember { mutableStateOf(null) }
    val closeSheet: () -> Unit = { currentBottomSheet = null }
    val openSheet: (BottomSheetScreen) -> Unit = { currentBottomSheet = it }

    // Root box fills whatever space the AnchoredDraggable gives it
    Box(modifier = Modifier.fillMaxSize()) {

        // Main player content
        PlayerBottomBar(
            onCollapsedClicked = onCollapsedClicked,
            bottomPadding = bottomPadding,
            currentFraction = currentFraction,
            isExtended = isExtended,
            songIcon = songIcon,
            artworkUrl = artworkUrl,
            title = title,
            musicPlayer = musicPlayer,
            onSkipNextPressed = onSkipNextPressed,
            onMoreClicked = { openSheet(BottomSheetScreen.More) },
            onBackgroundClicked = { closeSheet() },
            painterLoaded = { painter -> mutablePainter.value = painter },
            playBarMinimizedClicked = playBarMinimizedClicked,
            onFavoritePressed = onFavoritePressed,
            onSaveClicked = { openSheet(BottomSheetScreen.AddPlaylist) },
            newDominantColor = newDominantColor
        )

        // Secondary sheet overlay (More / AddPlaylist)
        AnimatedVisibility(
            visible = currentBottomSheet != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                animationSpec = tween(durationMillis = 300),
                initialOffsetY = { it }
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutVertically(
                animationSpec = tween(durationMillis = 250),
                targetOffsetY = { it }
            ) + fadeOut(animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
            ) {
                currentBottomSheet?.let { currentSheet ->
                    SheetLayout(
                        currentSheet,
                        closeSheet,
                        title,
                        mutablePainter,
                        openSheet,
                        addPlaylistList,
                        onAddPlaylistClicked,
                        getLatestPlaylist,
                        clickedToAddSongToPlaylist = { playlistTitle, playlistDescription, songList ->
                            closeSheet()
                            clickedToAddSongToPlaylist(
                                playlistTitle,
                                playlistDescription,
                                songList
                            )
                        },
                    )
                }
            }
        }
    }
}