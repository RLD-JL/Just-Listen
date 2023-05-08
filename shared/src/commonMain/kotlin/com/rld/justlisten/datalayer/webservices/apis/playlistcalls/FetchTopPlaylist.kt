package com.rld.justlisten.datalayer.webservices.apis.playlistcalls

import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.utils.Constants.appName
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

suspend fun ApiClient.fetchPlaylist(index: Int, playListEnum: PlayListEnum, playlistId: String="", queryPlaylist: String = "Rock"): PlayListResponse? {
   return when (playListEnum) {
       TOP_PLAYLIST -> getResponse("/full/playlists/top?type=playlist&limit=${index}&app_name=$appName")
       REMIX -> getResponse("/playlists/search?query=${queryPlaylist}&limit=${index}&app_name=$appName")
       CURRENT_PLAYLIST -> getResponse("/playlists/${playlistId}/tracks?app_name=$appName")
       HOT -> getResponse("/playlists/search?query=${queryPlaylist}&limit=${index}&app_name=$appName")
       FAVORITE -> TODO()
       CREATED_BY_USER -> TODO()
       MOST_PLAYED -> TODO()
   }
}

suspend fun ApiClient.getTracks(limit: Int, category: String, timeRange: String) : PlayListResponse? {
    return getResponse("/full/tracks/trending/EJ57D?genre=${category}&limit=${limit}&time=${timeRange}&app_name=$appName")
}

@Serializable
data class PlayListResponse(
    @SerialName("data") val data : List<PlayListModel>,
    @SerialName("err") val error : String? = null,
)