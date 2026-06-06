package com.rld.justlisten.datalayer.webservices.apis.playlistcalls

import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

suspend fun ApiClient.fetchPlaylist(
    index: Int,
    playListEnum: PlayListEnum,
    playlistId: String = "",
    queryPlaylist: String = "Rock"
): PlayListResponse? {
    return when (playListEnum) {
        TOP_PLAYLIST   -> getResponse("/full/playlists/top?type=playlist&limit=$index")
        REMIX          -> getResponse("/playlists/search?query=$queryPlaylist&limit=$index")
        CURRENT_PLAYLIST -> getResponse("/playlists/$playlistId/tracks")
        HOT            -> getResponse("/playlists/search?query=$queryPlaylist&limit=$index")
        FAVORITE, CREATED_BY_USER, MOST_PLAYED, TIME_CAPSULE -> null
    }
}

suspend fun ApiClient.getTracks(
    limit: Int,
    category: String,
    timeRange: String
): PlayListResponse? {
    val genreQuery = if (category.isEmpty() || category == "All") "" else "&genre=$category"
    val response: PlayListResponse? =
        getResponse("/full/tracks/trending?limit=$limit&time=$timeRange$genreQuery")
    return response?.let {
        PlayListResponse(it.data.filter { track -> track.isStreamable })
    }
}

@Serializable
data class PlayListResponse(
    @SerialName("data") val data: List<PlayListModel>,
    @SerialName("err") val error: String? = null,
)