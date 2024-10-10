package com.rld.justlisten.android.ui.bottombars.sheets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.painter.Painter
import com.rld.justlisten.android.ui.bottombars.playbar.components.addplaylist.AddPlaylistOption
import com.rld.justlisten.android.ui.bottombars.playbar.components.more.PlayBarMoreAction
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.viewmodel.Events

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
    events: Events
) {
    when (currentScreen) {
        BottomSheetScreen.AddPlaylist -> {
            AddPlaylistOption(
                title,
                mutablePainter,
                addPlaylistList,
                onAddPlaylistClicked,
                clickedToAddSongToPlaylist,
                events
            )
            getLatestPlaylist()
        }
        BottomSheetScreen.More -> PlayBarMoreAction(
            title,
            mutablePainter,
        ) {
            onCloseBottomSheet()
            openSheet(BottomSheetScreen.AddPlaylist)
        }
    }
}