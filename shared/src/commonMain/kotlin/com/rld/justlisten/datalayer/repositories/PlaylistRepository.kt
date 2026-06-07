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
import com.rld.justlisten.datalayer.webservices.apis.writecalls.repostTrack
import com.rld.justlisten.datalayer.webservices.apis.writecalls.unrepostTrack
import com.rld.justlisten.datalayer.webservices.apis.writecalls.repostPlaylist
import com.rld.justlisten.datalayer.webservices.apis.writecalls.unrepostPlaylist
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.screens.search.TrackItem
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

    fun isTrackReposted(id: String): Boolean
    fun setTrackReposted(id: String, reposted: Boolean)
    fun isPlaylistReposted(id: String): Boolean
    fun setPlaylistReposted(id: String, reposted: Boolean)
    suspend fun repostTrack(trackId: String): Boolean
    suspend fun unrepostTrack(trackId: String): Boolean
    suspend fun repostPlaylist(playlistId: String): Boolean
    suspend fun unrepostPlaylist(playlistId: String): Boolean

    val repostedTrackIdsFlow: kotlinx.coroutines.flow.StateFlow<Set<String>>
    val repostedPlaylistIdsFlow: kotlinx.coroutines.flow.StateFlow<Set<String>>
}

class PlaylistRepositoryImpl(
    private val localDb: LocalDb,
    private val webservices: ApiClient
) : PlaylistRepository {

    private val _repostedTrackIds = kotlinx.coroutines.flow.MutableStateFlow<Set<String>>(emptySet())
    override val repostedTrackIdsFlow = _repostedTrackIds.asStateFlow()

    private val _repostedPlaylistIds = kotlinx.coroutines.flow.MutableStateFlow<Set<String>>(emptySet())
    override val repostedPlaylistIdsFlow = _repostedPlaylistIds.asStateFlow()

    override fun isTrackReposted(id: String): Boolean {
        return _repostedTrackIds.value.contains(id)
    }

    override fun setTrackReposted(id: String, reposted: Boolean) {
        _repostedTrackIds.update { set ->
            if (reposted) set + id else set - id
        }
    }

    override fun isPlaylistReposted(id: String): Boolean {
        return _repostedPlaylistIds.value.contains(id)
    }

    override fun setPlaylistReposted(id: String, reposted: Boolean) {
        _repostedPlaylistIds.update { set ->
            if (reposted) set + id else set - id
        }
    }

    override suspend fun repostTrack(trackId: String): Boolean {
        val response = webservices.repostTrack(trackId)
        if (response?.error == null) {
            setTrackReposted(trackId, true)
            return true
        }
        return false
    }

    override suspend fun unrepostTrack(trackId: String): Boolean {
        val response = webservices.unrepostTrack(trackId)
        if (response?.error == null) {
            setTrackReposted(trackId, false)
            return true
        }
        return false
    }

    override suspend fun repostPlaylist(playlistId: String): Boolean {
        val response = webservices.repostPlaylist(playlistId)
        if (response?.error == null) {
            setPlaylistReposted(playlistId, true)
            return true
        }
        return false
    }

    override suspend fun unrepostPlaylist(playlistId: String): Boolean {
        val response = webservices.unrepostPlaylist(playlistId)
        if (response?.error == null) {
            setPlaylistReposted(playlistId, false)
            return true
        }
        return false
    }

    override suspend fun getPlaylist(
        index: Int,
        playListEnum: PlayListEnum,
        playlistId: String,
        songsList: List<String>,
        queryPlaylist: String
    ): List<PlaylistItem> {
        return when (playListEnum) {
            PlayListEnum.TOP_PLAYLIST -> webservices.fetchPlaylist(index, PlayListEnum.TOP_PLAYLIST)?.data?.map { playlistModel ->
                if (playlistModel.hasCurrentUserReposted) {
                    setTrackReposted(playlistModel.id, true)
                }
                val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
                val isFavorite = !hasFavorite.isNullOrEmpty()
                val isReposted = playlistModel.hasCurrentUserReposted || isTrackReposted(playlistModel.id)
                PlaylistItem(_data = playlistModel, isFavorite = isFavorite, isReposted = isReposted)
            } ?: emptyList()

            PlayListEnum.REMIX -> webservices.fetchPlaylist(
                index,
                PlayListEnum.REMIX,
                queryPlaylist = queryPlaylist
            )?.data?.map { playlistModel ->
                if (playlistModel.hasCurrentUserReposted) {
                    setTrackReposted(playlistModel.id, true)
                }
                val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
                val isFavorite = !hasFavorite.isNullOrEmpty()
                val isReposted = playlistModel.hasCurrentUserReposted || isTrackReposted(playlistModel.id)
                PlaylistItem(_data = playlistModel, isFavorite = isFavorite, isReposted = isReposted)
            } ?: emptyList()

            PlayListEnum.CURRENT_PLAYLIST -> {
                webservices.fetchPlaylist(
                    index,
                    PlayListEnum.CURRENT_PLAYLIST,
                    playlistId
                )?.data?.map { playlistModel ->
                    if (playlistModel.hasCurrentUserReposted) {
                        setTrackReposted(playlistModel.id, true)
                    }
                    val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
                    val isFavorite = !hasFavorite.isNullOrEmpty()
                    val isReposted = playlistModel.hasCurrentUserReposted || isTrackReposted(playlistModel.id)
                    PlaylistItem(_data = playlistModel, isFavorite = isFavorite, isReposted = isReposted)
                } ?: emptyList()
            }

            PlayListEnum.HOT -> webservices.fetchPlaylist(
                index,
                PlayListEnum.HOT,
                queryPlaylist = queryPlaylist
            )?.data?.map { playlistModel ->
                if (playlistModel.hasCurrentUserReposted) {
                    setTrackReposted(playlistModel.id, true)
                }
                val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
                val isFavorite = !hasFavorite.isNullOrEmpty()
                val isReposted = playlistModel.hasCurrentUserReposted || isTrackReposted(playlistModel.id)
                PlaylistItem(_data = playlistModel, isFavorite = isFavorite, isReposted = isReposted)
            } ?: emptyList()

            PlayListEnum.FAVORITE -> {
                localDb.getFavoritePlaylist().map { playlistModel ->
                    val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
                    val isFavorite = !hasFavorite.isNullOrEmpty()
                    val isReposted = playlistModel.hasCurrentUserReposted || isTrackReposted(playlistModel.id)
                    PlaylistItem(playlistModel, isFavorite = isFavorite, isReposted = isReposted)
                }.toList()
            }

            PlayListEnum.MOST_PLAYED -> {
                localDb.getMostPlayedSongs(20).map { playlistModel ->
                    val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
                    val isFavorite = !hasFavorite.isNullOrEmpty()
                    val isReposted = playlistModel.hasCurrentUserReposted || isTrackReposted(playlistModel.id)
                    PlaylistItem(playlistModel, isFavorite = isFavorite, isReposted = isReposted)
                }.toList()
            }

            PlayListEnum.CREATED_BY_USER -> {
                localDb.getCustomPlaylistSongs(songsList).map { playlistModel ->
                    val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
                    val isFavorite = !hasFavorite.isNullOrEmpty()
                    val isReposted = playlistModel.hasCurrentUserReposted || isTrackReposted(playlistModel.id)
                    PlaylistItem(playlistModel, isFavorite = isFavorite, isReposted = isReposted)
                }.toList()
            }

            PlayListEnum.TIME_CAPSULE -> {
                localDb.getTimeCapsuleSongs(20).map { playlistModel ->
                    val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
                    val isFavorite = !hasFavorite.isNullOrEmpty()
                    val isReposted = playlistModel.hasCurrentUserReposted || isTrackReposted(playlistModel.id)
                    PlaylistItem(playlistModel, isFavorite = isFavorite, isReposted = isReposted)
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
            if (playlistModel.hasCurrentUserReposted) {
                setTrackReposted(playlistModel.id, true)
            }
            val hasFavorite = localDb.getFavoritePlaylistWithId(playlistModel.id)
            val isFavorite = !hasFavorite.isNullOrEmpty()
            val isReposted = playlistModel.hasCurrentUserReposted || isTrackReposted(playlistModel.id)
            TrackItem(playlistModel, isFavorite = isFavorite, isReposted = isReposted)
        }?.toList() ?: emptyList()
    }

    override fun getSongWithId(songId: String): PlayListModel {
        return localDb.getSongWithId(songId)
    }
}
