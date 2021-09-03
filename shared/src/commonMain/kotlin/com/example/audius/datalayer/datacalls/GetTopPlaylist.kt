package com.example.audius.datalayer.datacalls

import com.example.audius.datalayer.Repository
import com.example.audius.datalayer.webservices.apis.playlistcalls.fetchTopPlaylist
import com.example.audius.viewmodel.screens.trending.PlaylistItem

suspend fun Repository.getTopPlaylist(): List<PlaylistItem> = webservices.fetchTopPlaylist()?.data?.map {
        playlistModel -> PlaylistItem(_data = playlistModel)
} ?: emptyList()

