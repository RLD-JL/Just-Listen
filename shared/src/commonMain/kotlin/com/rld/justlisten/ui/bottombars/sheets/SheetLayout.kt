package com.rld.justlisten.ui.bottombars.sheets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.painter.Painter
import com.rld.justlisten.ui.bottombars.playbar.components.addplaylist.AddPlaylistOption
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist

@Composable
fun SheetLayout(
    currentScreen: BottomSheetScreen,
    onCloseBottomSheet: () -> Unit,
    title: String,
    mutablePainter: MutableState<Painter?>,
    openSheet: (BottomSheetScreen) -> Unit,
    addPlaylistList: List<AddPlaylist>,
    onAddPlaylistClicked: (String, String?) -> Unit,
    getLatestPlaylist: () -> Unit,
    clickedToAddSongToPlaylist: (String, String?, List<String>) -> Unit,
    currentSongId: String? = null,
) {
    when (currentScreen) {
        BottomSheetScreen.AddPlaylist -> {
            AddPlaylistOption(
                title = title,
                addPlaylistList = addPlaylistList,
                onAddPlaylistClicked = onAddPlaylistClicked,
                clickedToAddSongToPlaylist = clickedToAddSongToPlaylist,
                currentSongId = currentSongId
            )
            getLatestPlaylist()
        }
    }
}
