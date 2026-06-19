package com.rld.justlisten.datalayer.webservices.apis.authcalls

import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.TokenResponse
import com.rld.justlisten.datalayer.models.PlayListModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import co.touchlab.kermit.Logger


@Serializable
data class ProfileImages(
    @SerialName("150x150") val image150: String? = null,
    @SerialName("480x480") val image480: String? = null,
    @SerialName("1000x1000") val image1000: String? = null,
    @SerialName("640x") val image640: String? = null,
    @SerialName("2000x") val image2000: String? = null
)

@Serializable
data class MeResponse(
    @SerialName("id") val userId: String? = null,
    @SerialName("name") val name: String,
    @SerialName("handle") val handle: String,
    @SerialName("verified") val verified: Boolean = false,
    @SerialName("profile_picture") val profilePicture: ProfileImages? = null
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
        "&redirect_uri=$redirectUri" +
        "&client_id=$apiKey"
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

suspend fun ApiClient.getUserFavoriteTracks(userId: String): List<PlayListModel> = coroutineScope {
    Logger.d { "ApiClient: Requesting GET /users/$userId/favorites for tracks" }
    val favorites = getUserFavorites(userId)
    val trackIds = favorites.filter { it.type.equals("SaveType.track", ignoreCase = true) || it.type.equals("track", ignoreCase = true) }.map { it.itemId }
    
    val chunkedTrackIds = trackIds.chunked(5)
    val fetchedTracks = mutableListOf<PlayListModel>()
    for (chunk in chunkedTrackIds) {
        val deferred = chunk.map { trackId ->
            async {
                try {
                    getTrackDetails(trackId)
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    Logger.e(e) { "ApiClient: Error fetching track details for favorite $trackId" }
                    null
                }
            }
        }
        fetchedTracks.addAll(deferred.awaitAll().filterNotNull())
    }
    fetchedTracks
}

suspend fun ApiClient.getUserPlaylists(userId: String): List<PlayListModel> {
    Logger.d { "ApiClient: Requesting GET /users/$userId/playlists" }
    val response: com.rld.justlisten.datalayer.webservices.apis.playlistcalls.PlayListResponse? = getResponse("/users/$userId/playlists")
    return response?.data ?: emptyList()
}

suspend fun ApiClient.getUserFavoritePlaylists(userId: String): List<PlayListModel> = coroutineScope {
    Logger.d { "ApiClient: Requesting GET /users/$userId/favorites for playlists" }
    val favorites = getUserFavorites(userId)
    val playlistIds = favorites.filter {
        it.type.equals("SaveType.playlist", ignoreCase = true) ||
        it.type.equals("SaveType.album", ignoreCase = true) ||
        it.type.equals("playlist", ignoreCase = true) ||
        it.type.equals("album", ignoreCase = true)
    }.map { it.itemId }
    
    val deferredPlaylists = playlistIds.map { playlistId ->
        async {
            try {
                val response: com.rld.justlisten.datalayer.webservices.apis.playlistcalls.PlayListResponse? = getResponse("/playlists/$playlistId")
                response?.data?.firstOrNull()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Logger.e(e) { "ApiClient: Error fetching playlist details for favorite $playlistId" }
                null
            }
        }
    }
    deferredPlaylists.awaitAll().filterNotNull()
}

@Serializable
data class UserProfileModel(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("handle") val handle: String = "",
    @SerialName("bio") val bio: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("profile_picture") val profilePicture: ProfileImages? = null,
    @SerialName("cover_photo") val coverPhoto: ProfileImages? = null,
    @SerialName("follower_count") val followerCount: Int = 0,
    @SerialName("followee_count") val followeeCount: Int = 0,
    @SerialName("track_count") val trackCount: Int = 0,
    @SerialName("playlist_count") val playlistCount: Int = 0,
    @SerialName("does_current_user_follow") val doesCurrentUserFollow: Boolean = false,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("twitter_handle") val twitterHandle: String? = null,
    @SerialName("instagram_handle") val instagramHandle: String? = null,
    @SerialName("tiktok_handle") val tiktokHandle: String? = null,
    @SerialName("website") val website: String? = null,
    @SerialName("coin_flair_mint") val coinFlairMint: String? = null,
    @SerialName("fan_club_flair") val fanClubFlair: String? = null
)

@Serializable
data class UserProfileResponse(
    @SerialName("data") val data: UserProfileModel
)

suspend fun ApiClient.getUserProfile(userId: String): UserProfileModel? {
    val response: UserProfileResponse? = getResponse("/users/$userId")
    return response?.data
}

@Serializable
data class FeedItemModel(
    @SerialName("type") val type: String,
    @SerialName("item") val item: PlayListModel
)

@Serializable
data class FeedResponse(
    @SerialName("data") val data: List<FeedItemModel>,
    @SerialName("err") val error: String? = null
)

suspend fun ApiClient.getUserFeed(
    userId: String,
    limit: Int = 20,
    offset: Int = 0,
    filter: String = "all",
    tracksOnly: Boolean? = null
): FeedResponse? {
    var url = "/users/$userId/feed?limit=$limit&offset=$offset"
    if (filter != "all") {
        url += "&filter=$filter"
    }
    if (tracksOnly != null) {
        url += "&tracks_only=$tracksOnly"
    }
    return getResponse(url)
}

@Serializable
data class UsersListResponse(
    @SerialName("data") val data: List<UserProfileModel> = emptyList()
)

suspend fun ApiClient.getUserFollowers(userId: String): List<UserProfileModel> {
    val response: UsersListResponse? = getResponse("/users/$userId/followers")
    return response?.data ?: emptyList()
}

suspend fun ApiClient.getUserFollowing(userId: String): List<UserProfileModel> {
    val response: UsersListResponse? = getResponse("/users/$userId/following")
    return response?.data ?: emptyList()
}

@Serializable
data class UserCoinModel(
    @SerialName("mint") val mint: String,
    @SerialName("ticker") val ticker: String,
    @SerialName("decimals") val decimals: Int,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("balance") val balance: String,
    @SerialName("balance_usd") val balanceUsd: Double? = null,
    @SerialName("logo_uri") val logoUri: String? = null,
    @SerialName("banner_image_url") val bannerImageUrl: String? = null,
    @SerialName("has_discord") val hasDiscord: Boolean = false
)

@Serializable
data class UserCoinsResponse(
    @SerialName("data") val data: List<UserCoinModel> = emptyList()
)

suspend fun ApiClient.getUserCoins(userId: String): List<UserCoinModel> {
    val response: UserCoinsResponse? = getResponse("/users/$userId/coins")
    return response?.data ?: emptyList()
}


