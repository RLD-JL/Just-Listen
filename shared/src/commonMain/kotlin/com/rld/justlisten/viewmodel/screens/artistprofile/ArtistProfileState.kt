package com.rld.justlisten.viewmodel.screens.artistprofile

import com.rld.justlisten.ScreenState
import com.rld.justlisten.datalayer.webservices.apis.authcalls.UserProfileModel
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.datalayer.models.PlayListModel

data class ArtistProfileState(
    val isLoading: Boolean = false,
    val artistProfile: UserProfileModel? = null,
    val artistTracks: List<PlaylistItem> = emptyList(),
    val artistPlaylists: List<PlayListModel> = emptyList(),
    val selectedTabIndex: Int = 0,
    val showConnectPrompt: Boolean = false,
    val isCurrentUser: Boolean = false
) : ScreenState
