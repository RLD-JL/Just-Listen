package com.example.justlisten.datalayer.webservices.apis.playlistcalls

import com.example.justlisten.datalayer.models.PlayListModel
import com.example.justlisten.datalayer.webservices.ApiClient
import com.example.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.example.justlisten.viewmodel.screens.playlist.PlayListEnum.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
suspend fun ApiClient.fetchPlaylist(index: Int, playListEnum: PlayListEnum, playlistId: String=""): PlayListResponse? {
   return when (playListEnum) {
       TOP_PLAYLIST -> getResponse("/playlists/top?type=playlist&limit=${index}&app_name=ExampleApp")
       REMIX -> getResponse("/playlists/search?query=Remixes&app_name=ExampleApp")
       CURRENT_PLAYLIST -> getResponse("/playlists/${playlistId}/tracks?app_name=EXAMPLEAPP ")
       HOT -> TODO()
       FAVORITE -> TODO()
       CREATED_BY_USER -> TODO()
   }
}


@Serializable
data class PlayListResponse(
    @SerialName("data") val data : List<PlayListModel>,
    @SerialName("err") val error : String? = null,
)