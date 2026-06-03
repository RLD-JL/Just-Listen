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

interface LibraryRepository {
    fun saveSongToRecent(
        id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String
    )
    fun saveSongToMostPlayed(
        id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String
    )
    fun getMostPlayedSongs(numberOfLines: Long): List<PlayListModel>
    fun getRecentSongs(numberOfLines: Long): List<PlayListModel>
    fun getTimeCapsuleSongs(limit: Long): List<PlayListModel>

    fun insertPlayLog(songId: String, timestamp: Long, durationPlayedSec: Long, completed: Boolean)
    fun getTotalPlays(): Long
    fun getUniquePlays(): Long
    fun getTotalDurationPlayed(): Long
    fun getDurationPlayedForSong(songId: String): Long
    fun getDurationPlayedForArtist(user: UserModel): Long
    fun getMostPlayedSongsFromHistory(limit: Long, offset: Long): List<PlayListModel>
    fun getTopArtistFromHistory(): Triple<UserModel, Long, Long>?
    fun getPlayHistoryFlow(): Flow<Unit>
    
    // Add Playlist functions
    fun savePlaylist(playlistName: String, playlistDescription: String?)
    fun getAddPlaylist(): List<AddPlaylist>
    fun getAddPlaylistFlow(): Flow<List<AddPlaylist>>
    fun updatePlaylistSongs(playlistName: String, playlistDescription: String?, songList: List<String>)
    fun deletePlaylist(playlistName: String)
    fun updatePlaylistName(oldName: String, newName: String)
}

class LibraryRepositoryImpl(
    private val localDb: LocalDb
) : LibraryRepository {
    private val _playHistoryFlow = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)

    override fun saveSongToRecent(
        id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String
    ) {
        localDb.saveSongRecentSongs(id, title, user, songImgList, playlistName)
    }

    override fun saveSongToMostPlayed(
        id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String
    ) {
        localDb.saveMostPlayedSongs(id, title, user, songImgList, playlistName)
    }

    override fun getMostPlayedSongs(numberOfLines: Long): List<PlayListModel> {
        return localDb.getMostPlayedSongs(numberOfLines)
    }

    override fun getRecentSongs(numberOfLines: Long): List<PlayListModel> {
        return localDb.getRecentPlayed(numberOfLines)
    }

    override fun getTimeCapsuleSongs(limit: Long): List<PlayListModel> {
        return localDb.getTimeCapsuleSongs(limit)
    }

    override fun insertPlayLog(songId: String, timestamp: Long, durationPlayedSec: Long, completed: Boolean) {
        localDb.insertPlayLog(songId, timestamp, durationPlayedSec, completed)
        _playHistoryFlow.tryEmit(Unit)
    }

    override fun getPlayHistoryFlow(): Flow<Unit> = _playHistoryFlow.asSharedFlow()

    override fun getTotalPlays(): Long {
        return localDb.getTotalPlaysFromHistory()
    }

    override fun getUniquePlays(): Long {
        return localDb.getUniquePlayedCountFromHistory()
    }

    override fun getTotalDurationPlayed(): Long {
        return localDb.getTotalDurationPlayedFromHistory()
    }

    override fun getDurationPlayedForSong(songId: String): Long {
        return localDb.getDurationPlayedForSongFromHistory(songId)
    }

    override fun getDurationPlayedForArtist(user: UserModel): Long {
        return localDb.getDurationPlayedForArtistFromHistory(user)
    }

    override fun getMostPlayedSongsFromHistory(limit: Long, offset: Long): List<PlayListModel> {
        return localDb.getMostPlayedSongsFromHistory(limit, offset)
    }

    override fun getTopArtistFromHistory(): Triple<UserModel, Long, Long>? {
        return localDb.getTopArtistFromHistory()
    }

    override fun savePlaylist(playlistName: String, playlistDescription: String?) {
        localDb.savePlaylist(playlistName, playlistDescription)
    }

    override fun getAddPlaylist(): List<AddPlaylist> {
        return localDb.getAddPlaylist()
    }

    override fun getAddPlaylistFlow(): Flow<List<AddPlaylist>> {
        return localDb.getAddPlaylistFlow()
    }

    override fun updatePlaylistSongs(
        playlistName: String,
        playlistDescription: String?,
        songList: List<String>
    ) {
        localDb.updatePlaylistSongs(playlistName, playlistDescription, songList)
    }

    override fun deletePlaylist(playlistName: String) {
        localDb.deletePlaylist(playlistName)
    }

    override fun updatePlaylistName(oldName: String, newName: String) {
        localDb.updatePlaylistName(oldName, newName)
    }
}

