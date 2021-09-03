package com.example.audius.datalayer.datacalls

import com.example.audius.datalayer.Repository
import com.example.audius.datalayer.webservices.apis.playlistcalls.fetchRemixPlaylist
import com.example.audius.datalayer.webservices.apis.playlistcalls.fetchTopPlaylist
import com.example.audius.viewmodel.screens.trending.PlaylistItem

suspend fun Repository.getTopPlaylist(index: Int): List<PlaylistItem> = webservices.fetchTopPlaylist(index)?.data?.map {
        playlistModel -> PlaylistItem(_data = playlistModel)
} ?: emptyList()

suspend fun Repository.getRemixPlaylist(index: Int): List<PlaylistItem> = webservices.fetchRemixPlaylist(index)?.data?.map {
        playlistModel -> PlaylistItem(_data = playlistModel)
} ?: emptyList()
