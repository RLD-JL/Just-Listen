package com.rld.justlisten.android.ui.bottombars.sheets

sealed class BottomSheetScreen {
    object More: BottomSheetScreen()
    object AddPlaylist: BottomSheetScreen()
}
