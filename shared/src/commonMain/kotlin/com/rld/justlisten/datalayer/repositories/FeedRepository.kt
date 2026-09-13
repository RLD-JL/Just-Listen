package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserFeed
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException

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
            val favoriteIds = withContext(Dispatchers.IO) { localDb.libraryQueries.getFavoriteIds().executeAsList().toSet() }
            response?.data?.map { feedItem ->
                val playlistModel = feedItem.item.copy(
                    isPlaylist = feedItem.type == "playlist" || feedItem.item.isPlaylist
                )
                // Seed the repost cache if the API says this user has reposted it
                if (playlistModel.hasCurrentUserReposted) {
                    playlistRepository.setTrackReposted(playlistModel.id, true)
                }
                val isFavorite = playlistModel.id in favoriteIds
                val isReposted = playlistModel.hasCurrentUserReposted || playlistRepository.isTrackReposted(playlistModel.id)
                PlaylistItem(_data = playlistModel, isFavorite = isFavorite, isReposted = isReposted)
            } ?: emptyList()
        }.getOrElse { if (it is CancellationException) throw it else emptyList() }
    }
}
