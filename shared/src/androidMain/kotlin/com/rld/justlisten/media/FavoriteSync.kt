package com.rld.justlisten.media

fun updateFavorite(
    isFavorite: Boolean,
    musicPlayer: MusicPlayer,
    songId: String,
) {
    if (musicPlayer is AndroidMusicPlayer) {
        musicPlayer.musicServiceConnection.isFavorite[songId] = isFavorite
    }
}
