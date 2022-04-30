package com.example.justlisten.viewmodel.screens.playlistdetail

import com.example.justlisten.viewmodel.Events

fun Events.saveDominantColor(color: Int) {
    stateManager.updateScreen(PlaylistDetailState::class) {
    it.copy(dominantColor = color)
    }
}