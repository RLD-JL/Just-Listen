package com.example.justlisten.datalayer.datacalls.playlist

import com.example.justlisten.datalayer.Repository
import com.example.justlisten.datalayer.localdb.libraryscreen.getCustomPlaylistSongs
import com.example.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylist
import com.example.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylistWithId
import com.example.justlisten.datalayer.localdb.playlistdetail.getPlaylistDetail
import com.example.justlisten.datalayer.webservices.apis.playlistcalls.fetchPlaylist
import com.example.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.example.justlisten.viewmodel.screens.playlist.PlayListEnum.*
import com.example.justlisten.viewmodel.screens.playlist.PlaylistItem

suspend fun Repository.getPlaylist(index: Int, playListEnum: PlayListEnum, playlistId: String= "DOPRl",
songsList: List<String> = emptyList()): List<PlaylistItem> {
    return when (playListEnum) {

        TOP_PLAYLIST -> webservices.fetchPlaylist(index, TOP_PLAYLIST)?.data?.map { playlistModel ->
            PlaylistItem(_data = playlistModel)
        } ?: emptyList()

        REMIX -> webservices.fetchPlaylist(index, REMIX)?.data?.map { playlistModel ->
            PlaylistItem(_data = playlistModel)
        } ?: emptyList()

        CURRENT_PLAYLIST -> { webservices.fetchPlaylist(index, CURRENT_PLAYLIST, playlistId)?.data?.map { playlistModel ->
            val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
            val isFavorite = !hasFavorite.isNullOrEmpty()
            PlaylistItem(_data = playlistModel, isFavorite)
        } ?: emptyList()}

        HOT -> {
        localDb.getPlaylistDetail().map {
                elem->PlaylistItem(_data = elem)
        }.toList() }
        FAVORITE -> {
            localDb.getFavoritePlaylist().map {playlistModel ->
                val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
                val isFavorite = !hasFavorite.isNullOrEmpty()
                PlaylistItem(playlistModel, isFavorite)
            }.toList()
        }
        CREATED_BY_USER -> {
            localDb.getCustomPlaylistSongs(songsList).map {playlistModel ->
                val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
                val isFavorite = !hasFavorite.isNullOrEmpty()
                PlaylistItem(playlistModel, isFavorite)
            }.toList()
        }
    }
}

suspend fun Repository.getCurrentPlaylist(): List<PlaylistItem> = withRepoContext {
    localDb.getPlaylistDetail().map {
            elem->PlaylistItem(_data = elem)
    }.toList()
}
