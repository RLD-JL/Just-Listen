package com.example.audius.viewmodel.screens.playlistdetail

import com.example.audius.Navigation
import com.example.audius.ScreenParams
import com.example.audius.datalayer.datacalls.playlist.getPlaylist
import com.example.audius.viewmodel.screens.ScreenInitSettings
import com.example.audius.viewmodel.screens.playlist.PlayListEnum
import kotlinx.serialization.Serializable

data class PlaylistDetailParams(
    val playlistId: String,
    val playlistIcon: String,
    val playlistTitle: String,
    val playlistCreatedBy: String,
    val playlistEnum: PlayListEnum,
    val painter: Any
) : ScreenParams

fun Navigation.initPlaylistDetail(params: PlaylistDetailParams) = ScreenInitSettings(
    title = "PlaylistEnum" + params.playlistIcon,
    initState = { PlaylistDetailState(isLoading = true) },
    callOnInit = {
        val currentPlaylist =
            dataRepository.getPlaylist(index = 20, params.playlistEnum, params.playlistId)
        stateManager.updateScreen(PlaylistDetailState::class) {
            it.copy(
                isLoading = false,
                playlistIcon = params.playlistIcon,
                playListCreatedBy = params.playlistCreatedBy,
                playlistName = params.playlistTitle,
                songPlaylist = currentPlaylist
            )
        }
    },
    reinitOnEachNavigation = true
)