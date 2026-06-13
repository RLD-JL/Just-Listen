package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylistWithId
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserFeed
import com.rld.justlisten.datalayer.webservices.apis.authcalls.FeedResponse
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

interface FeedRepository {
    suspend fun getUserFeed(
        userId: String,
        limit: Int = 20,
        offset: Int = 0,
        filter: String = "all",
        tracksOnly: Boolean? = null
    ): List<PlaylistItem>
}

class FeedRepositoryImpl(
    private val localDb: LocalDb,
    private val webservices: ApiClient,
    private val playlistRepository: PlaylistRepository
) : FeedRepository {

    override suspend fun getUserFeed(
        userId: String,
        limit: Int,
        offset: Int,
        filter: String,
        tracksOnly: Boolean?
    ): List<PlaylistItem> {
        return runCatching {
            val response = webservices.getUserFeed(userId, limit, offset, filter, tracksOnly)
            response?.data?.map { feedItem ->
                val playlistModel = feedItem.item.copy(
                    isPlaylist = feedItem.type == "playlist" || feedItem.item.isPlaylist
                )
                // Seed the repost cache if the API says this user has reposted it
                if (playlistModel.hasCurrentUserReposted) {
                    playlistRepository.setTrackReposted(playlistModel.id, true)
                }
                val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
                val isFavorite = !hasFavorite.isNullOrEmpty()
                val isReposted = playlistModel.hasCurrentUserReposted || playlistRepository.isTrackReposted(playlistModel.id)
                PlaylistItem(_data = playlistModel, isFavorite = isFavorite, isReposted = isReposted)
            } ?: emptyList()
        }.getOrElse { emptyList() }
    }
}
