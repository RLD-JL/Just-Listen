package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.LocalDb
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.deletePlaylist
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.getAddPlaylist
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.savePlaylist
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.updatePlaylistSongs
import com.rld.justlisten.datalayer.localdb.libraryscreen.getMostPlayedSongs
import com.rld.justlisten.datalayer.localdb.libraryscreen.getRecentPlayed
import com.rld.justlisten.datalayer.localdb.libraryscreen.getSongWithId
import com.rld.justlisten.datalayer.localdb.libraryscreen.saveMostPlayedSongs
import com.rld.justlisten.datalayer.localdb.libraryscreen.saveSongRecentSongs
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel

interface LibraryRepository {
    fun saveSongToRecent(
        id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String
    )
    fun saveSongToMostPlayed(
        id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String
    )
    fun getMostPlayedSongs(numberOfLines: Long): List<PlayListModel>
    fun getRecentSongs(numberOfLines: Long): List<PlayListModel>
    
    // Add Playlist functions
    fun savePlaylist(playlistName: String, playlistDescription: String?)
    fun getAddPlaylist(): List<AddPlaylist>
    fun updatePlaylistSongs(playlistName: String, playlistDescription: String?, songList: List<String>)
    fun deletePlaylist(playlistName: String)
}

class LibraryRepositoryImpl(
    private val localDb: LocalDb
) : LibraryRepository {

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

    override fun savePlaylist(playlistName: String, playlistDescription: String?) {
        localDb.savePlaylist(playlistName, playlistDescription)
    }

    override fun getAddPlaylist(): List<AddPlaylist> {
        return localDb.getAddPlaylist()
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
}
