package com.example.justlisten.viewmodel.screens.playlistdetail

import com.example.justlisten.Navigation
import com.example.justlisten.ScreenParams
import com.example.justlisten.datalayer.datacalls.playlist.getPlaylist
import com.example.justlisten.viewmodel.screens.ScreenInitSettings
import com.example.justlisten.viewmodel.screens.playlist.PlayListEnum
import kotlinx.serialization.Serializable

data class PlaylistDetailParams(
    val playlistId: String,
    val playlistIcon: String,
    val playlistTitle: String,
    val playlistCreatedBy: String,
    val playlistEnum: PlayListEnum,
    val songsList: List<String> = emptyList()
) : ScreenParams

fun com.example.justlisten.Navigation.initPlaylistDetail(params: PlaylistDetailParams) = ScreenInitSettings(
    title = "PlaylistEnum" + params.playlistIcon,
    initState = { PlaylistDetailState(isLoading = true) },
    callOnInit = {
        val currentPlaylist =
            dataRepository.getPlaylist(
                index = 20,
                params.playlistEnum,
                params.playlistId,
                params.songsList
            )

        val playlistIcon =
            if (params.playlistEnum == PlayListEnum.CREATED_BY_USER && currentPlaylist.isNotEmpty())
                currentPlaylist[0].songIconList.songImageURL480px else params.playlistIcon

        stateManager.updateScreen(PlaylistDetailState::class) {
            it.copy(
                isLoading = false,
                playlistIcon = playlistIcon,
                playListCreatedBy = params.playlistCreatedBy,
                playlistName = params.playlistTitle,
                songPlaylist = currentPlaylist
            )
        }
    },
    reinitOnEachNavigation = true
)