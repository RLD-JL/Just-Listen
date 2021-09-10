package com.example.audius.datalayer.datacalls

import com.example.audius.datalayer.Repository
import com.example.audius.datalayer.webservices.apis.playlistcalls.fetchPlaylist
import com.example.audius.viewmodel.screens.trending.PlayListEnum
import com.example.audius.viewmodel.screens.trending.PlayListEnum.*
import com.example.audius.viewmodel.screens.trending.PlaylistItem

suspend fun Repository.getPlaylist(index: Int, playListEnum: PlayListEnum): List<PlaylistItem> {
    return when (playListEnum) {

        TOP_PLAYLIST -> webservices.fetchPlaylist(index, TOP_PLAYLIST)?.data?.map { playlistModel ->
            PlaylistItem(_data = playlistModel)
        } ?: emptyList()

        REMIX -> webservices.fetchPlaylist(index, REMIX)?.data?.map { playlistModel ->
            PlaylistItem(_data = playlistModel)
        } ?: emptyList()

        CURRENT_PLAYLIST -> webservices.fetchPlaylist(index, CURRENT_PLAYLIST)?.data?.map { playlistModel ->
            PlaylistItem(_data = playlistModel)
        } ?: emptyList()
        HOT -> TODO()
    }
}
