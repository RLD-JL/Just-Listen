package com.example.audius.datalayer.webservices.apis.playlistcalls

import com.example.audius.datalayer.models.PlayListModel
import com.example.audius.datalayer.models.TrendingListModel
import com.example.audius.datalayer.webservices.ApiClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

suspend fun ApiClient.fetchTopPlaylist(): PlayListResponse? {
   return getResponse("/playlists/top?type=playlist&app_name=ExampleApp")
}

@Serializable
data class PlayListResponse(
    @SerialName("data") val data : List<PlayListModel>,
    @SerialName("err") val error : String? = null,
)