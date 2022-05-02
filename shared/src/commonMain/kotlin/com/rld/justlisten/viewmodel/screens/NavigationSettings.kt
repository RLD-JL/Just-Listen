package com.rld.justlisten.viewmodel.screens

import com.rld.justlisten.ScreenIdentifier
import com.rld.justlisten.viewmodel.screens.library.LibraryParams
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistParams
import com.rld.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailParams

object navigationSettings {
    val homeScreen = Level1Navigation.Playlist // the start screen should be specified here
    val saveLastLevel1Screen = false
    val alwaysQuitOnHomeScreen = true
}


// LEVEL 1 NAVIGATION OF THE APP

enum class Level1Navigation(
    val screenIdentifier: ScreenIdentifier,
    val rememberVerticalStack: Boolean = true
) {
    Library(ScreenIdentifier.get(Screen.Library, LibraryParams("")), true),
    Playlist(ScreenIdentifier.get(Screen.Playlist, PlaylistParams("")), true),
    Fund(ScreenIdentifier.get(Screen.Fund, null), true),
    Settings(ScreenIdentifier.get(Screen.Settings, null), true)
}