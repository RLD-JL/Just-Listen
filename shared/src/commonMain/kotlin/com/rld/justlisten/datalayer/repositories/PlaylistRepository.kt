package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.localdb.libraryscreen.getCustomPlaylistSongs
import com.rld.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylist
import com.rld.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylistWithId
import com.rld.justlisten.datalayer.localdb.libraryscreen.getMostPlayedSongs
import com.rld.justlisten.datalayer.localdb.libraryscreen.getSongWithId
import com.rld.justlisten.datalayer.localdb.libraryscreen.getTimeCapsuleSongs
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.playlistcalls.fetchPlaylist
import com.rld.justlisten.datalayer.webservices.apis.playlistcalls.getTracks
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.screens.search.TrackItem

interface PlaylistRepository {
    suspend fun getPlaylist(
        index: Int,
        playListEnum: PlayListEnum,
        playlistId: String = "DOPRl",
        songsList: List<String> = emptyList(),
        queryPlaylist: String = "Rock"
    ): List<PlaylistItem>

    suspend fun getTracks(limit: Int, category: String, timeRange: String): List<TrackItem>
    
    fun getSongWithId(songId: String): PlayListModel
}

class PlaylistRepositoryImpl(
    private val localDb: LocalDb,
    private val webservices: ApiClient
) : PlaylistRepository {

    override suspend fun getPlaylist(
        index: Int,
        playListEnum: PlayListEnum,
        playlistId: String,
        songsList: List<String>,
        queryPlaylist: String
    ): List<PlaylistItem> {
        return when (playListEnum) {
            PlayListEnum.TOP_PLAYLIST -> webservices.fetchPlaylist(index, PlayListEnum.TOP_PLAYLIST)?.data?.map { playlistModel ->
                PlaylistItem(_data = playlistModel)
            } ?: emptyList()

            PlayListEnum.REMIX -> webservices.fetchPlaylist(
                index,
                PlayListEnum.REMIX,
                queryPlaylist = queryPlaylist
            )?.data?.map { playlistModel ->
                PlaylistItem(_data = playlistModel)
            } ?: emptyList()

            PlayListEnum.CURRENT_PLAYLIST -> {
                webservices.fetchPlaylist(
                    index,
                    PlayListEnum.CURRENT_PLAYLIST,
                    playlistId
                )?.data?.map { playlistModel ->
                    val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
                    val isFavorite = !hasFavorite.isNullOrEmpty()
                    PlaylistItem(_data = playlistModel, isFavorite)
                } ?: emptyList()
            }

            PlayListEnum.HOT -> webservices.fetchPlaylist(
                index,
                PlayListEnum.HOT,
                queryPlaylist = queryPlaylist
            )?.data?.map { playlistModel ->
                PlaylistItem(_data = playlistModel)
            } ?: emptyList()

            PlayListEnum.FAVORITE -> {
                localDb.getFavoritePlaylist().map { playlistModel ->
                    val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
                    val isFavorite = !hasFavorite.isNullOrEmpty()
                    PlaylistItem(playlistModel, isFavorite)
                }.toList()
            }

            PlayListEnum.MOST_PLAYED -> {
                localDb.getMostPlayedSongs(20).map { playlistModel ->
                    val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
                    val isFavorite = !hasFavorite.isNullOrEmpty()
                    PlaylistItem(playlistModel, isFavorite)
                }.toList()
            }

            PlayListEnum.CREATED_BY_USER -> {
                localDb.getCustomPlaylistSongs(songsList).map { playlistModel ->
                    val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
                    val isFavorite = !hasFavorite.isNullOrEmpty()
                    PlaylistItem(playlistModel, isFavorite)
                }.toList()
            }

            PlayListEnum.TIME_CAPSULE -> {
                localDb.getTimeCapsuleSongs(20).map { playlistModel ->
                    val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
                    val isFavorite = !hasFavorite.isNullOrEmpty()
                    PlaylistItem(playlistModel, isFavorite)
                }.toList()
            }
        }
    }

    override suspend fun getTracks(
        limit: Int,
        category: String,
        timeRange: String
    ): List<TrackItem> {
        return webservices.getTracks(limit, category, timeRange)?.data?.map { playlistModel ->
            TrackItem(playlistModel)
        }?.toList() ?: emptyList()
    }

    override fun getSongWithId(songId: String): PlayListModel {
        return localDb.getSongWithId(songId)
    }
}
