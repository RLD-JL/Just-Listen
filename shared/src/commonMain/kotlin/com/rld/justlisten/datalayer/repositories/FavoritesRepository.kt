package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylist
import com.rld.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylistFlow
import com.rld.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylistWithId
import com.rld.justlisten.datalayer.localdb.libraryscreen.saveSongToFavorites
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun saveSongToFavorites(
        id: String,
        title: String,
        user: UserModel,
        songImgList: SongIconList,
        playlistName: String,
        isFavorite: Boolean
    )
    fun getFavoritePlaylist(): List<PlayListModel>
    fun getFavoritePlaylistWithId(id: String): String?
    fun getFavoritePlaylistFlow(): Flow<List<PlayListModel>>
}

class FavoritesRepositoryImpl(
    private val localDb: LocalDb
) : FavoritesRepository {

    override fun saveSongToFavorites(
        id: String,
        title: String,
        user: UserModel,
        songImgList: SongIconList,
        playlistName: String,
        isFavorite: Boolean
    ) {
        localDb.saveSongToFavorites(id, title, user, songImgList, playlistName, isFavorite)
    }

    override fun getFavoritePlaylist(): List<PlayListModel> {
        return localDb.getFavoritePlaylist()
    }

    override fun getFavoritePlaylistWithId(id: String): String? {
        return localDb.getFavoritePlaylistWithId(id)
    }

    override fun getFavoritePlaylistFlow(): Flow<List<PlayListModel>> {
        return localDb.getFavoritePlaylistFlow()
    }
}
