package com.rld.justlisten.ui.utils

import com.rld.justlisten.ui.theme.ColorPallet
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.viewmodel.interfaces.Item
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.screens.search.TrackItem
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val loggingScope = CoroutineScope(Dispatchers.Default)

fun getColorPallet(pallet: String): ColorPallet {
    return when (pallet) {
        "Dark" -> ColorPallet.Dark
        "Green" -> ColorPallet.Green
        "Purple" -> ColorPallet.Purple
        "Blue" -> ColorPallet.Blue
        "Orange" -> ColorPallet.Orange
        "Pink" -> ColorPallet.Pink
        "Custom" -> ColorPallet.Custom
        "Expressive" -> ColorPallet.Expressive
        else -> ColorPallet.Dark
    }
}

fun playMusicFromId(
    musicPlayer: MusicPlayer,
    playlist: List<Item>,
    songId: String,
    repository: LibraryRepository? = null,
    playlistId: String? = null
) {
    musicPlayer.currentlyPlayingPlaylistId = playlistId
    // Save to recent tracking
    repository?.let { repo ->
        playlist.find { it.id == songId }?.let { song ->
            val userModel = when (song) {
                is TrackItem -> song._data.user
                is PlaylistItem -> song._data.user
                else -> UserModel(username = song.user, id = song.userId)
            }
            loggingScope.launch {
                repo.saveSongToRecent(
                    song.id, song.title,
                    userModel,
                    song.songIconList, song.playlistTitle
                )
            }
        }
    }
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
    repository: LibraryRepository? = null,
    playlistId: String? = null
) {
    if (playlist.isEmpty()) return
    musicPlayer.currentlyPlayingPlaylistId = playlistId
    musicPlayer.updatePlaylist(playlist)
    val mediaId = playFromId.ifEmpty { playlist.first().id }
    // Save to recent tracking
    repository?.let { repo ->
        playlist.find { it.id == mediaId }?.let { song ->
            val userModel = when (song) {
                is TrackItem -> song._data.user
                is PlaylistItem -> song._data.user
                else -> UserModel(username = song.user, id = song.userId)
            }
            loggingScope.launch {
                repo.saveSongToRecent(
                    song.id, song.title,
                    userModel,
                    song.songIconList, song.playlistTitle
                )
            }
        }
    }
    musicPlayer.playMedia(mediaId)
}
