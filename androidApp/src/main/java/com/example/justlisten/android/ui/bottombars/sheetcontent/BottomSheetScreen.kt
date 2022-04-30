package com.example.justlisten.android.ui.bottombars.sheetcontent

sealed class BottomSheetScreen {
    object More: BottomSheetScreen()
    object AddPlaylist: BottomSheetScreen()
}
