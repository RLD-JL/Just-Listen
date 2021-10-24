package com.example.audius.viewmodel.screens.playlist

import com.example.audius.datalayer.datacalls.getPlaylist
import com.example.audius.datalayer.models.SongIconList
import com.example.audius.viewmodel.Events
import com.example.audius.viewmodel.screens.playlist.PlayListEnum.*

fun Events.playMusic(songId: String, songIcon: SongIconList) = screenCoroutine{

    stateManager.updateScreen(TrendingListState::class) {
        it.copy(playMusic = true, songId = songId, songIcon = songIcon.songImageURL150px)
    }
}

fun Events.skipToNextSong() = screenCoroutine{
    stateManager.updateScreen(TrendingListState::class) {
        it.copy()
    }
}

fun Events.fetchPlaylist(index: Int, playlistEnum: PlayListEnum) = screenCoroutine {
    stateManager.updateScreen(PlaylistState::class) {
        when(playlistEnum) {
            TOP_PLAYLIST ->  it.copy(playlistItems = dataRepository.getPlaylist(index, playlistEnum))
            REMIX ->  it.copy(remixPlaylist = dataRepository.getPlaylist(index, playlistEnum))
            HOT -> TODO()
            CURRENT_PLAYLIST -> TODO()
        }
    }
}

fun Events.playMusicFromPlaylist(playlistId: String) {
    currentPlaylistId = playlistId
}
