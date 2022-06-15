package com.rld.justlisten.android.ui.utils

import android.support.v4.media.MediaBrowserCompat
import com.rld.justlisten.android.exoplayer.MusicServiceConnection
import com.rld.justlisten.android.exoplayer.utils.Constants
import com.rld.justlisten.android.ui.theme.ColorPallet
import com.rld.justlisten.viewmodel.interfaces.Item

fun getColorPallet(pallet: String): ColorPallet {
    return when(pallet) {
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
    musicServiceConnection: MusicServiceConnection,
    playlist: List<Item>,
    songId: String,
    isPlayerReady: Boolean
) {
    if (isPlayerReady) {
        musicServiceConnection.transportControls.playFromMediaId(songId, null)
    } else {
        playMusic(musicServiceConnection, playlist, isPlayerReady, songId)
    }
}

fun playMusic(
    musicServiceConnection: MusicServiceConnection,
    playlist: List<Item>,
    isPlayerReady: Boolean,
    playFromId: String = ""
) {
    if (!isPlayerReady) {
        musicServiceConnection.updatePlaylist(playlist)
        musicServiceConnection.subscribe(
            Constants.CLICKED_PLAYLIST,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }
    if (playFromId != "") {
        musicServiceConnection.transportControls.playFromMediaId(playFromId, null)
    }
}
