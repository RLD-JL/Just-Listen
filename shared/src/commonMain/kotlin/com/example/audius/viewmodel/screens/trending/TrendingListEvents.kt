package com.example.audius.viewmodel.screens.trending

import com.example.audius.viewmodel.Events

fun Events.playMusic(songId: String) = screenCoroutine{

    stateManager.updateScreen(TrendingListState::class) {
        it.copy(playMusic = true, songId = songId)
    }

}