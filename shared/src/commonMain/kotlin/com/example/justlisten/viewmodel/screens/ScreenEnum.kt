package com.example.justlisten.viewmodel.screens

import com.example.justlisten.Navigation
import com.example.justlisten.ScreenIdentifier
import com.example.justlisten.viewmodel.screens.addplaylist.initAddPlaylist
import com.example.justlisten.viewmodel.screens.library.initLibrary
import com.example.justlisten.viewmodel.screens.playlist.initPlaylist
import com.example.justlisten.viewmodel.screens.playlistdetail.initPlaylistDetail
import com.example.justlisten.viewmodel.screens.search.initSearch

enum class Screen (
    val asString: String,
    val navigationLevel : Int = 1,
    val initSettings: com.example.justlisten.Navigation.(ScreenIdentifier) -> ScreenInitSettings,
    val stackableInstances : Boolean = true,
) {
    Library("library", 1, {initLibrary(it.params())}),
    Playlist("playlist", 1, {initPlaylist(it.params())}),
    PlaylistDetail("playlistDetail", 1, {initPlaylistDetail(it.params())}),
    AddPlaylist("addPlaylist", 1, {initAddPlaylist(it.params())}),
    Search("screen", 1, {initSearch()})
}