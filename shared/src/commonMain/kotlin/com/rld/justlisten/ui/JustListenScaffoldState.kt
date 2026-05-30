package com.rld.justlisten.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.datacalls.addplaylistscreen.getAddPlaylist
import com.rld.justlisten.datalayer.datacalls.addplaylistscreen.savePlaylist
import com.rld.justlisten.datalayer.datacalls.addplaylistscreen.updatePlaylistSongs
import com.rld.justlisten.datalayer.datacalls.library.saveSongToFavorites
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.media.MusicPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class JustListenScaffoldState(
    val repository: Repository,
    val musicPlayer: MusicPlayer,
    val scope: CoroutineScope
) {
    var addPlaylistList by mutableStateOf(emptyList<AddPlaylist>())
        private set

    fun loadAddPlaylists() {
        scope.launch {
            addPlaylistList = repository.getAddPlaylist()
        }
    }

    fun saveSongToFavorites(
        id: String, title: String, user: UserModel, songIcon: SongIconList, isFavorite: Boolean
    ) {
        scope.launch {
            repository.saveSongToFavorites(id, title, user, songIcon, "Favorite", isFavorite)
            musicPlayer.refreshMetadata()
        }
    }

    fun savePlaylist(name: String, description: String?) {
        scope.launch {
            repository.savePlaylist(name, description)
            loadAddPlaylists()
        }
    }

    fun updatePlaylistSongs(title: String, description: String?, songs: List<String>) {
        scope.launch {
            repository.updatePlaylistSongs(title, description, songs)
        }
    }
}

@Composable
fun rememberJustListenScaffoldState(
    repository: Repository,
    musicPlayer: MusicPlayer,
    scope: CoroutineScope
): JustListenScaffoldState {
    return remember(repository, musicPlayer, scope) {
        JustListenScaffoldState(repository, musicPlayer, scope)
    }
}
