package com.example.audius.viewmodel.screens

import com.example.audius.Navigation
import com.example.audius.ScreenIdentifier
import com.example.audius.viewmodel.screens.addplaylist.initAddPlaylist
import com.example.audius.viewmodel.screens.library.initLibrary
import com.example.audius.viewmodel.screens.playlist.initPlaylist
import com.example.audius.viewmodel.screens.playlistdetail.initPlaylistDetail
import com.example.audius.viewmodel.screens.search.initSearch

enum class Screen (
    val asString: String,
    val navigationLevel : Int = 1,
    val initSettings: Navigation.(ScreenIdentifier) -> ScreenInitSettings,
    val stackableInstances : Boolean = true,
) {
    Library("library", 1, {initLibrary(it.params())}),
    Playlist("playlist", 1, {initPlaylist(it.params())}),
    PlaylistDetail("playlistDetail", 1, {initPlaylistDetail(it.params())}),
    AddPlaylist("playlist", 1, {initAddPlaylist(it.params())}),
    Search("screen", 1, {initSearch()})
}