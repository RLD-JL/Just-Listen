package com.rld.justlisten.viewmodel.player

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val libraryRepository: LibraryRepository,
) : BaseScreenViewModel() {

    private val _addPlaylistList = MutableStateFlow(emptyList<AddPlaylist>())
    val addPlaylistList: StateFlow<List<AddPlaylist>> = _addPlaylistList.asStateFlow()

    fun loadAddPlaylists() {
        viewModelScope.launch {
            _addPlaylistList.value = libraryRepository.getAddPlaylist()
        }
    }

    fun saveSongToFavorites(
        id: String, title: String, user: UserModel, songIcon: SongIconList, isFavorite: Boolean, musicPlayer: MusicPlayer
    ) {
        viewModelScope.launch {
            favoritesRepository.saveSongToFavorites(id, title, user, songIcon, "Favorite", isFavorite)
            musicPlayer.refreshMetadata()
        }
    }

    fun savePlaylist(name: String, description: String?) {
        viewModelScope.launch {
            libraryRepository.savePlaylist(name, description)
            loadAddPlaylists()
        }
    }

    fun updatePlaylistSongs(title: String, description: String?, songs: List<String>) {
        viewModelScope.launch {
            libraryRepository.updatePlaylistSongs(title, description, songs)
        }
    }
}
