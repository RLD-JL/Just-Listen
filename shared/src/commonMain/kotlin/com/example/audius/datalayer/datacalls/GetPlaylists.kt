package com.example.audius.datalayer.datacalls

import com.example.audius.datalayer.Repository
import com.example.audius.datalayer.localdb.getTrendingList
import com.example.audius.datalayer.localdb.playlistdetail.getPlaylistDetail
import com.example.audius.datalayer.localdb.playlistdetail.setPlaylistDetail
import com.example.audius.datalayer.localdb.setTrendingList
import com.example.audius.datalayer.webservices.apis.playlistcalls.fetchPlaylist
import com.example.audius.datalayer.webservices.apis.trendingcalls.fetchTrendingList
import com.example.audius.viewmodel.screens.trending.PlayListEnum
import com.example.audius.viewmodel.screens.trending.PlayListEnum.*
import com.example.audius.viewmodel.screens.trending.PlaylistItem
import com.example.audius.viewmodel.screens.trending.TrendingListItem

suspend fun Repository.getPlaylist(index: Int, playListEnum: PlayListEnum, playlistId: String= ""): List<PlaylistItem> {
    return when (playListEnum) {

        TOP_PLAYLIST -> webservices.fetchPlaylist(index, TOP_PLAYLIST)?.data?.map { playlistModel ->
            PlaylistItem(_data = playlistModel)
        } ?: emptyList()

        REMIX -> webservices.fetchPlaylist(index, REMIX)?.data?.map { playlistModel ->
            PlaylistItem(_data = playlistModel)
        } ?: emptyList()

        CURRENT_PLAYLIST -> webservices.fetchPlaylist(index, CURRENT_PLAYLIST, playlistId)?.apply {
            if(error==null) {
                localDb.setPlaylistDetail(data)
            }}?.data?.map { playlistModel ->
            PlaylistItem(_data = playlistModel)

        } ?: emptyList()
        HOT -> {
        localDb.getPlaylistDetail().map {
                elem->PlaylistItem(_data = elem)
        }.toList() }
    }
}

suspend fun Repository.getCurrentPlaylist(): List<PlaylistItem> = withRepoContext {
    localDb.getPlaylistDetail().map {
            elem->PlaylistItem(_data = elem)
    }.toList()
}
