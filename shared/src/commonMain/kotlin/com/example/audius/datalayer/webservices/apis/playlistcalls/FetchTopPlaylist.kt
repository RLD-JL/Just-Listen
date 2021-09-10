package com.example.audius.datalayer.webservices.apis.playlistcalls

import com.example.audius.datalayer.models.PlayListModel
import com.example.audius.datalayer.models.TrendingListModel
import com.example.audius.datalayer.webservices.ApiClient
import com.example.audius.viewmodel.screens.trending.PlayListEnum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

suspend fun ApiClient.fetchTopPlaylist(index: Int): PlayListResponse? {
   return getResponse("/playlists/top?type=playlist&limit=${index}&app_name=ExampleApp")
}

suspend fun ApiClient.fetchRemixPlaylist(index: Int): PlayListResponse? {
    return getResponse("/playlists/search?query=Remixables&app_name=EXAMPLEAPP")
}

suspend fun ApiClient.fetchPlaylist(index: Int, playListEnum: PlayListEnum): PlayListResponse? {
   return when (playListEnum) {
       PlayListEnum.TOP_PLAYLIST -> getResponse("/playlists/top?type=playlist&limit=${index}&app_name=ExampleApp")
       PlayListEnum.REMIX -> getResponse("/playlists/search?query=Remixes&app_name=ExampleApp")
       PlayListEnum.CURRENT_PLAYLIST -> getResponse("/playlists/DOPRl/tracks?app_name=EXAMPLEAPP ")
       PlayListEnum.HOT -> TODO()
   }
}


@Serializable
data class PlayListResponse(
    @SerialName("data") val data : List<PlayListModel>,
    @SerialName("err") val error : String? = null,
)