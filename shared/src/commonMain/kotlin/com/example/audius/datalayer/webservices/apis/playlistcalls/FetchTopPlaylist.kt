package com.example.audius.datalayer.webservices.apis.playlistcalls

import com.example.audius.datalayer.models.PlayListModel
import com.example.audius.datalayer.models.TrendingListModel
import com.example.audius.datalayer.webservices.ApiClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

suspend fun ApiClient.fetchTopPlaylist(index: Int): PlayListResponse? {
   return getResponse("/playlists/top?type=playlist&limit=${index}&app_name=ExampleApp")
}

suspend fun ApiClient.fetchRemixPlaylist(index: Int): PlayListResponse? {
    return getResponse("//playlists/search?query=Remixables&app_name=EXAMPLEAPP")
}
@Serializable
data class PlayListResponse(
    @SerialName("data") val data : List<PlayListModel>,
    @SerialName("err") val error : String? = null,
)