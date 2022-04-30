package com.rld.justlisten.viewmodel.screens.playlistdetail

import com.rld.justlisten.viewmodel.Events

fun Events.saveDominantColor(color: Int) {
    stateManager.updateScreen(PlaylistDetailState::class) {
    it.copy(dominantColor = color)
    }
}