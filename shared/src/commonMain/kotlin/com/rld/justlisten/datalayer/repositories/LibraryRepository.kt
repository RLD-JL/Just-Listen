package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.LocalDb
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.deletePlaylist
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.getAddPlaylist
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.getAddPlaylistFlow
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.savePlaylist
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.updatePlaylistName
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.updatePlaylistSongs
import com.rld.justlisten.datalayer.localdb.libraryscreen.*
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asSharedFlow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

interface LibraryRepository {
    suspend fun saveSongToRecent(
        id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String
    )
    suspend fun saveSongToMostPlayed(
        id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String
    )
    suspend fun getMostPlayedSongs(numberOfLines: Long): List<PlayListModel>
    suspend fun getRecentSongs(numberOfLines: Long): List<PlayListModel>
    suspend fun getTimeCapsuleSongs(limit: Long): List<PlayListModel>

    suspend fun insertPlayLog(songId: String, timestamp: Long, durationPlayedSec: Long, completed: Boolean)
    suspend fun getTotalPlays(): Long
    suspend fun getUniquePlays(): Long
    suspend fun getTotalDurationPlayed(): Long
    suspend fun getDurationPlayedForSong(songId: String): Long
    suspend fun getDurationPlayedForArtist(user: UserModel): Long
    suspend fun getMostPlayedSongsFromHistory(limit: Long, offset: Long): List<PlayListModel>
    suspend fun getTopArtistFromHistory(): Triple<UserModel, Long, Long>?
    fun getPlayHistoryFlow(): Flow<Unit>
    
    // Add Playlist functions
    suspend fun savePlaylist(playlistName: String, playlistDescription: String?, isRemote: Boolean = false, isPrivate: Boolean = false, playlistId: String? = null)
    suspend fun getAddPlaylist(): List<AddPlaylist>
    fun getAddPlaylistFlow(): Flow<List<AddPlaylist>>
    suspend fun updatePlaylistSongs(playlistName: String, playlistDescription: String?, songList: List<String>, isRemote: Boolean = false, isPrivate: Boolean = false, playlistId: String? = null)
    suspend fun deletePlaylist(playlistName: String)
    suspend fun updatePlaylistName(oldName: String, newName: String)
}

class LibraryRepositoryImpl(
    private val localDb: LocalDb,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) : LibraryRepository {
    private val _playHistoryFlow = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)

    override suspend fun saveSongToRecent(
        id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String
    ) = withContext(Dispatchers.IO) {
        localDb.saveSongRecentSongs(id, title, user, songImgList, playlistName)
    }

    override suspend fun saveSongToMostPlayed(
        id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String
    ) = withContext(Dispatchers.IO) {
        localDb.saveMostPlayedSongs(id, title, user, songImgList, playlistName)
    }

    override suspend fun getMostPlayedSongs(numberOfLines: Long): List<PlayListModel> = withContext(Dispatchers.IO) {
        localDb.getMostPlayedSongs(numberOfLines)
    }

    override suspend fun getRecentSongs(numberOfLines: Long): List<PlayListModel> = withContext(Dispatchers.IO) {
        localDb.getRecentPlayed(numberOfLines)
    }

    override suspend fun getTimeCapsuleSongs(limit: Long): List<PlayListModel> = withContext(Dispatchers.IO) {
        localDb.getTimeCapsuleSongs(limit)
    }

    override suspend fun insertPlayLog(songId: String, timestamp: Long, durationPlayedSec: Long, completed: Boolean) = withContext(Dispatchers.IO) {
        localDb.insertPlayLog(songId, timestamp, durationPlayedSec, completed)
        _playHistoryFlow.tryEmit(Unit)
        Unit
    }

    override fun getPlayHistoryFlow(): Flow<Unit> = _playHistoryFlow.asSharedFlow()

    override suspend fun getTotalPlays(): Long = withContext(Dispatchers.IO) {
        localDb.getTotalPlaysFromHistory()
    }

    override suspend fun getUniquePlays(): Long = withContext(Dispatchers.IO) {
        localDb.getUniquePlayedCountFromHistory()
    }

    override suspend fun getTotalDurationPlayed(): Long = withContext(Dispatchers.IO) {
        localDb.getTotalDurationPlayedFromHistory()
    }

    override suspend fun getDurationPlayedForSong(songId: String): Long = withContext(Dispatchers.IO) {
        localDb.getDurationPlayedForSongFromHistory(songId)
    }

    override suspend fun getDurationPlayedForArtist(user: UserModel): Long = withContext(Dispatchers.IO) {
        localDb.getDurationPlayedForArtistFromHistory(user)
    }

    override suspend fun getMostPlayedSongsFromHistory(limit: Long, offset: Long): List<PlayListModel> = withContext(Dispatchers.IO) {
        localDb.getMostPlayedSongsFromHistory(limit, offset)
    }

    override suspend fun getTopArtistFromHistory(): Triple<UserModel, Long, Long>? = withContext(Dispatchers.IO) {
        localDb.getTopArtistFromHistory()
    }

    override suspend fun savePlaylist(playlistName: String, playlistDescription: String?, isRemote: Boolean, isPrivate: Boolean, playlistId: String?) = withContext(Dispatchers.IO) {
        localDb.savePlaylist(playlistName, playlistDescription, isRemote, isPrivate, playlistId)
    }

    override suspend fun getAddPlaylist(): List<AddPlaylist> = withContext(Dispatchers.IO) {
        localDb.getAddPlaylist()
    }

    override fun getAddPlaylistFlow(): Flow<List<AddPlaylist>> {
        return localDb.getAddPlaylistFlow()
    }

    override suspend fun updatePlaylistSongs(
        playlistName: String,
        playlistDescription: String?,
        songList: List<String>,
        isRemote: Boolean,
        isPrivate: Boolean,
        playlistId: String?
    ) = withContext(Dispatchers.IO) {
        localDb.updatePlaylistSongs(playlistName, playlistDescription, songList, isRemote, isPrivate, playlistId)
        
        val playlist = localDb.addPlaylistQueries.getPlaylistByName(playlistName).executeAsOneOrNull()
        if (playlist != null && playlist.isRemote && !playlist.playlistId.isNullOrBlank()) {
            if (authRepository.sessionState.value is SessionState.Authenticated) {
                syncRepository.enqueuePlaylistUpdateTask(playlist.playlistId, songList)
            }
        }
    }

    override suspend fun deletePlaylist(playlistName: String) = withContext(Dispatchers.IO) {
        val playlist = localDb.addPlaylistQueries.getPlaylistByName(playlistName).executeAsOneOrNull()
        localDb.deletePlaylist(playlistName)
        if (playlist != null && playlist.isRemote && !playlist.playlistId.isNullOrBlank()) {
            if (authRepository.sessionState.value is SessionState.Authenticated) {
                syncRepository.enqueuePlaylistDeleteTask(playlist.playlistId)
            }
        }
    }

    override suspend fun updatePlaylistName(oldName: String, newName: String) = withContext(Dispatchers.IO) {
        localDb.updatePlaylistName(oldName, newName)
        val playlist = localDb.addPlaylistQueries.getPlaylistByName(newName).executeAsOneOrNull()
        if (playlist != null && playlist.isRemote && !playlist.playlistId.isNullOrBlank()) {
            if (authRepository.sessionState.value is SessionState.Authenticated) {
                syncRepository.enqueuePlaylistDetailsUpdateTask(playlist.playlistId, newName, playlist.playlistDescription)
            }
        }
    }
}

