package com.example.audius.viewmodel.screens.playlistdetail

import com.example.audius.viewmodel.Events

fun Events.saveDominantColor(color: Int) {
    stateManager.updateScreen(PlaylistDetailState::class) {
    it.copy(dominantColor = color)
    }
}