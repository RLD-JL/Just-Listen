package com.example.audius.viewmodel.screens.trending

import com.example.audius.datalayer.datacalls.getTopPlaylist
import com.example.audius.datalayer.models.SongIconList
import com.example.audius.viewmodel.Events

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

fun Events.fetchMorePlaylistItems(index: Int) = screenCoroutine {
    stateManager.updateScreen(PlaylistState::class) {
        it.copy(playlistItems = dataRepository.getTopPlaylist(index))
    }
}