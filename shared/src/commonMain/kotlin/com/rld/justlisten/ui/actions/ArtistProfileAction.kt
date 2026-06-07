package com.rld.justlisten.ui.actions

sealed interface ArtistProfileAction {
    data object BackPressed : ArtistProfileAction
    data class SongPressed(val songId: String) : ArtistProfileAction
    data class PlaylistClicked(
        val playlistId: String,
        val playlistIcon: String,
        val createdBy: String,
        val title: String
    ) : ArtistProfileAction
    data object FollowPressed : ArtistProfileAction
    data object DismissConnectPrompt : ArtistProfileAction
    data object ConnectAudiusPressed : ArtistProfileAction
    data class TabSelected(val index: Int) : ArtistProfileAction
}
