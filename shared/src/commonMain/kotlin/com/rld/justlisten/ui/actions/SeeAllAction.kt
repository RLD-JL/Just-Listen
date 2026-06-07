package com.rld.justlisten.ui.actions

sealed interface SeeAllAction {
    data class PlaylistClicked(
        val playlistId: String,
        val playlistIcon: String,
        val createdBy: String,
        val title: String,
    ) : SeeAllAction

    data object BackPressed : SeeAllAction

    data class LoadMore(val offset: Int) : SeeAllAction


    data class SongPressed(
        val songId: String,
        val title: String,
        val user: String,
        val songIconList: com.rld.justlisten.datalayer.models.SongIconList
    ) : SeeAllAction
    
    data class ArtistClicked(
        val artistId: String,
        val artistName: String
    ) : SeeAllAction
}
