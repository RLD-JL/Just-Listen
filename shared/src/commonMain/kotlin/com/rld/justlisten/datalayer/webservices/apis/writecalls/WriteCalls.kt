package com.rld.justlisten.datalayer.webservices.apis.writecalls

import com.rld.justlisten.datalayer.webservices.ApiClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteResponse(
    @SerialName("status") val status: String? = null,
    @SerialName("error") val error: String? = null
)

@Serializable
data class PlaylistCreatePayload(
    @SerialName("playlist_name") val playlistName: String,
    @SerialName("is_private") val isPrivate: Boolean,
    @SerialName("description") val description: String? = null
)

@Serializable
data class PlaylistCreateResponse(
    @SerialName("playlist_id") val playlistId: String? = null,
    @SerialName("error") val error: String? = null
)

suspend fun ApiClient.favoriteTrack(trackId: String): FavoriteResponse? {
    return postResponse("/tracks/$trackId/favorite")
}

suspend fun ApiClient.unfavoriteTrack(trackId: String): FavoriteResponse? {
    return deleteResponse("/tracks/$trackId/favorite")
}

suspend fun ApiClient.createPlaylist(
    name: String,
    description: String?,
    isPrivate: Boolean
): PlaylistCreateResponse? {
    return postResponse(
        "/playlists",
        PlaylistCreatePayload(name, isPrivate, description)
    )
}
