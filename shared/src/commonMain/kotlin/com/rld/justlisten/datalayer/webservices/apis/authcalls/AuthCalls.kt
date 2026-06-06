package com.rld.justlisten.datalayer.webservices.apis.authcalls

import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.TokenResponse
import com.rld.justlisten.datalayer.models.PlayListModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ProfileImages(
    @SerialName("150x150") val image150: String? = null,
    @SerialName("480x480") val image480: String? = null,
    @SerialName("1000x1000") val image1000: String? = null
)

@Serializable
data class MeResponse(
    @SerialName("id") val userId: String? = null,
    @SerialName("name") val name: String,
    @SerialName("handle") val handle: String,
    @SerialName("verified") val verified: Boolean = false,
    @SerialName("profilePicture") val profilePicture: ProfileImages? = null
)

suspend fun ApiClient.exchangeCodeForTokens(
    code: String,
    codeVerifier: String,
    redirectUri: String
): TokenResponse? {
    return postResponse(
        "/oauth/token",
        "grant_type=authorization_code" +
        "&code=$code" +
        "&code_verifier=$codeVerifier" +
        "&redirect_uri=$redirectUri"
    )
}

@Serializable
data class UserResponse(
    @SerialName("data") val data: MeResponse
)

suspend fun ApiClient.getMe(): MeResponse? {
    val response: UserResponse? = getResponse("/me")
    return response?.data
}

@Serializable
data class CloudFavorite(
    @SerialName("favorite_item_id") val itemId: String,
    @SerialName("favorite_type") val type: String,
    @SerialName("user_id") val userId: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class UserFavoritesResponse(
    @SerialName("data") val data: List<CloudFavorite>
)

suspend fun ApiClient.getUserFavorites(userId: String): List<CloudFavorite> {
    val response: UserFavoritesResponse? = getResponse("/users/$userId/favorites")
    return response?.data ?: emptyList()
}

@Serializable
data class TrackDetailsResponse(
    @SerialName("data") val data: PlayListModel
)

suspend fun ApiClient.getTrackDetails(trackId: String): PlayListModel? {
    val response: TrackDetailsResponse? = getResponse("/tracks/$trackId")
    return response?.data
}

suspend fun ApiClient.getUserFavoriteTracks(userId: String): List<PlayListModel> {
    println("ApiClient: Requesting GET /users/$userId/favorites for tracks")
    val favorites = getUserFavorites(userId)
    val trackIds = favorites.filter { it.type == "SaveType.track" || it.type == "track" }.map { it.itemId }
    val tracks = mutableListOf<PlayListModel>()
    for (trackId in trackIds) {
        try {
            val track = getTrackDetails(trackId)
            if (track != null) {
                tracks.add(track)
            }
            kotlinx.coroutines.delay(100L)
        } catch (e: Exception) {
            println("ApiClient: Error fetching track details for favorite $trackId: ${e.message}")
        }
    }
    return tracks
}

suspend fun ApiClient.getUserPlaylists(userId: String): List<PlayListModel> {
    println("ApiClient: Requesting GET /users/$userId/playlists")
    val response: com.rld.justlisten.datalayer.webservices.apis.playlistcalls.PlayListResponse? = getResponse("/users/$userId/playlists")
    return response?.data ?: emptyList()
}

suspend fun ApiClient.getUserFavoritePlaylists(userId: String): List<PlayListModel> {
    println("ApiClient: Requesting GET /users/$userId/favorites for playlists")
    val favorites = getUserFavorites(userId)
    val playlistIds = favorites.filter { it.type == "SaveType.playlist" || it.type == "SaveType.album" || it.type == "playlist" || it.type == "album" }.map { it.itemId }
    val playlists = mutableListOf<PlayListModel>()
    for (playlistId in playlistIds) {
        try {
            val response: com.rld.justlisten.datalayer.webservices.apis.playlistcalls.PlayListResponse? = getResponse("/playlists/$playlistId")
            val playlist = response?.data?.firstOrNull()
            if (playlist != null) {
                playlists.add(playlist)
            }
            kotlinx.coroutines.delay(100L)
        } catch (e: Exception) {
            println("ApiClient: Error fetching playlist details for favorite $playlistId: ${e.message}")
        }
    }
    return playlists
}
