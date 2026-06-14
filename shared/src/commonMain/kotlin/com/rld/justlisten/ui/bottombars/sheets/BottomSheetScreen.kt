package com.rld.justlisten.ui.bottombars.sheets

sealed class BottomSheetScreen {
    object AddPlaylist: BottomSheetScreen()
    data class Comments(val trackId: String): BottomSheetScreen()
}
