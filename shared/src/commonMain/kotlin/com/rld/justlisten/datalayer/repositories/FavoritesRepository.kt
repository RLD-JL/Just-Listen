package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.LocalDb
import com.rld.justlisten.util.authSessionLock
import com.rld.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylist
import com.rld.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylistFlow
import com.rld.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylistWithId
import com.rld.justlisten.datalayer.localdb.libraryscreen.saveSongToFavorites
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

interface FavoritesRepository {
    suspend fun saveSongToFavorites(
        id: String,
        title: String,
        user: UserModel,
        songImgList: SongIconList,
        playlistName: String,
        isFavorite: Boolean
    )
    suspend fun getFavoritePlaylist(): List<PlayListModel>
    suspend fun getFavoritePlaylistWithId(id: String): String?
    fun getFavoritePlaylistFlow(): Flow<List<PlayListModel>>
}

class FavoritesRepositoryImpl(
    private val localDb: LocalDb,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FavoritesRepository {

    override suspend fun saveSongToFavorites(
        id: String,
        title: String,
        user: UserModel,
        songImgList: SongIconList,
        playlistName: String,
        isFavorite: Boolean
    ) = withContext(ioDispatcher) {
        authSessionLock.withLock {
            localDb.transaction {
                localDb.saveSongToFavorites(id, title, user, songImgList, playlistName, isFavorite)
                val session = authRepository.sessionState.value
                if (session is SessionState.Authenticated) {
                    session.userProfile.userId?.takeIf { it.isNotBlank() }?.let { userId ->
                        syncRepository.enqueueFavoriteTask(userId, id, isFavorite)
                    }
                }
            }
        }
    }

    override suspend fun getFavoritePlaylist(): List<PlayListModel> = withContext(Dispatchers.IO) {
        localDb.getFavoritePlaylist()
    }

    override suspend fun getFavoritePlaylistWithId(id: String): String? = withContext(Dispatchers.IO) {
        localDb.getFavoritePlaylistWithId(id)
    }

    override fun getFavoritePlaylistFlow(): Flow<List<PlayListModel>> {
        return localDb.getFavoritePlaylistFlow()
    }
}
