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
    return postResponse("/tracks/$trackId/favorites")
}

suspend fun ApiClient.unfavoriteTrack(trackId: String): FavoriteResponse? {
    return deleteResponse("/tracks/$trackId/favorites")
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

suspend fun ApiClient.followUser(userId: String): FavoriteResponse? {
    return postResponse("/users/$userId/follow")
}

suspend fun ApiClient.unfollowUser(userId: String): FavoriteResponse? {
    return deleteResponse("/users/$userId/follow")
}

suspend fun ApiClient.repostTrack(trackId: String): FavoriteResponse? {
    return postResponse("/tracks/$trackId/reposts")
}

suspend fun ApiClient.unrepostTrack(trackId: String): FavoriteResponse? {
    return deleteResponse("/tracks/$trackId/reposts")
}

suspend fun ApiClient.repostPlaylist(playlistId: String): FavoriteResponse? {
    return postResponse("/playlists/$playlistId/reposts")
}

suspend fun ApiClient.unrepostPlaylist(playlistId: String): FavoriteResponse? {
    return deleteResponse("/playlists/$playlistId/reposts")
}

@Serializable
data class PlaylistAddedTimestamp(
    @SerialName("track_id") val trackId: String,
    @SerialName("timestamp") val timestamp: Int
)

@Serializable
data class UpdatePlaylistPayload(
    @SerialName("playlist_contents") val playlistContents: List<PlaylistAddedTimestamp>
)

suspend fun ApiClient.updatePlaylistSongs(
    playlistId: String,
    songList: List<String>
): FavoriteResponse? {
    val timestamp = kotlin.time.Clock.System.now().epochSeconds.toInt()
    val contents = songList.map { PlaylistAddedTimestamp(it, timestamp) }
    return putResponse("/playlists/$playlistId", UpdatePlaylistPayload(contents))
}

suspend fun ApiClient.deletePlaylist(
    playlistId: String
): FavoriteResponse? {
    return deleteResponse("/playlists/$playlistId")
}

@Serializable
data class UpdatePlaylistDetailsPayload(
    @SerialName("playlist_name") val playlistName: String,
    @SerialName("description") val description: String? = null
)

suspend fun ApiClient.updatePlaylistDetails(
    playlistId: String,
    name: String,
    description: String?
): FavoriteResponse? {
    return putResponse("/playlists/$playlistId", UpdatePlaylistDetailsPayload(name, description))
}

