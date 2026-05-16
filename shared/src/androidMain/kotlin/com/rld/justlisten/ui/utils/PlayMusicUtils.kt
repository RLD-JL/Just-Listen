package com.rld.justlisten.ui.utils

import com.rld.justlisten.ui.theme.ColorPallet
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.viewmodel.interfaces.Item

fun getColorPallet(pallet: String): ColorPallet {
    return when (pallet) {
        "Dark" -> ColorPallet.Dark
        "Green" -> ColorPallet.Green
        "Purple" -> ColorPallet.Purple
        "Blue" -> ColorPallet.Blue
        "Orange" -> ColorPallet.Orange
        "Pink" -> ColorPallet.Pink
        else -> ColorPallet.Dark
    }
}

fun playMusicFromId(
    musicPlayer: MusicPlayer,
    playlist: List<Item>,
    songId: String,
) {
    if (playlist.isNotEmpty()) {
        musicPlayer.updatePlaylist(playlist)
    }
    if (songId.isNotEmpty()) {
        musicPlayer.playMedia(songId)
    }
}

fun playMusic(
    musicPlayer: MusicPlayer,
    playlist: List<Item>,
    playFromId: String = "",
) {
    if (playlist.isEmpty()) return
    musicPlayer.updatePlaylist(playlist)
    val mediaId = playFromId.ifEmpty { playlist.first().id }
    musicPlayer.playMedia(mediaId)
}
