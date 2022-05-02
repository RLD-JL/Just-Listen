package com.rld.justlisten.viewmodel.screens

import com.rld.justlisten.Navigation
import com.rld.justlisten.ScreenIdentifier
import com.rld.justlisten.viewmodel.screens.addplaylist.initAddPlaylist
import com.rld.justlisten.viewmodel.screens.library.initLibrary
import com.rld.justlisten.viewmodel.screens.playlist.initPlaylist
import com.rld.justlisten.viewmodel.screens.playlistdetail.initPlaylistDetail
import com.rld.justlisten.viewmodel.screens.search.initSearch
import com.rld.justlisten.viewmodel.screens.settings.initSettings

enum class Screen (
    val asString: String,
    val navigationLevel : Int = 1,
    val initSettings: Navigation.(ScreenIdentifier) -> ScreenInitSettings,
    val stackableInstances : Boolean = true,
) {
    Library("library", 1, {initLibrary(it.params())}),
    Playlist("playlist", 1, {initPlaylist(it.params())}),
    PlaylistDetail("playlistDetail", 1, {initPlaylistDetail(it.params())}),
    AddPlaylist("addPlaylist", 1, {initAddPlaylist(it.params())}),
    Search("screen", 1, {initSearch()}),
    Fund("fund", 1, {initSearch()}),
    Settings("settings", 1, {initSettings()})
}