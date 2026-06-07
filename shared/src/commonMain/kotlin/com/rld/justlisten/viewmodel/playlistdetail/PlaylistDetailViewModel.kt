package com.rld.justlisten.viewmodel.playlistdetail

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.datalayer.repositories.PlaylistRepository
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailState
import kotlinx.coroutines.flow.update
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.rld.justlisten.datalayer.repositories.AuthRepository

class PlaylistDetailViewModel(
    private val playlistRepository: PlaylistRepository,
    private val favoritesRepository: FavoritesRepository,
    private val libraryRepository: LibraryRepository,
    private val authRepository: AuthRepository,
) : BaseScreenViewModel() {

    private val _playlistDetailState = MutableStateFlow(PlaylistDetailState(isLoading = true))
    val playlistDetailState: StateFlow<PlaylistDetailState> = _playlistDetailState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository.getFavoritePlaylistFlow().collect { favoriteList ->
                val favoriteIds = favoriteList.map { it.id }.toSet()
                _playlistDetailState.update { state ->
                    val updated = state.songPlaylist.map { item ->
                        item.copy(isFavorite = favoriteIds.contains(item.id))
                    }
                    state.copy(songPlaylist = updated)
                }
            }
        }
        viewModelScope.launch {
            playlistRepository.repostedTrackIdsFlow.collect { repostedIds ->
                _playlistDetailState.update { state ->
                    val updated = state.songPlaylist.map { item ->
                        item.copy(isReposted = repostedIds.contains(item.id))
                    }
                    state.copy(songPlaylist = updated)
                }
            }
        }
    }

    fun onFavoritePressed(
        id: String, title: String, user: UserModel, songIconList: SongIconList,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            favoritesRepository.saveSongToFavorites(id, title, user, songIconList, "Favorite", isFavorite)
            // Update in-memory state so UI reflects the change immediately
            _playlistDetailState.update { state ->
                state.copy(
                    songPlaylist = state.songPlaylist.map { item ->
                        if (item.id == id) {
                            item.copy(isFavorite = isFavorite)
                        } else {
                            item
                        }
                    }
                )
            }
        }
    }

    fun load(args: Route.PlaylistDetail) {
        if (_playlistDetailState.value.playlistName == args.playlistTitle &&
            _playlistDetailState.value.songPlaylist.isNotEmpty()
        ) {
            return
        }
        viewModelScope.launch {
            _playlistDetailState.update { 
                it.copy(
                    isLoading = true, 
                    playlistName = args.playlistTitle, 
                    playListCreatedBy = args.playlistCreatedBy, 
                    playlistIcon = args.playlistIcon,
                    playlistId = args.playlistId
                ) 
            }
            val playlistEnum = PlayListEnum.valueOf(args.playlistEnum)
            val songs = playlistRepository.getPlaylist(
                index = 0, 
                playListEnum = playlistEnum, 
                playlistId = args.playlistId,
                songsList = args.songsList
            )
            _playlistDetailState.update {
                it.copy(
                    isLoading = false,
                    songPlaylist = songs,
                )
            }
        }
    }

    fun deletePlaylist(playlistName: String) {
        viewModelScope.launch {
            libraryRepository.deletePlaylist(playlistName)
            popBackStack()
        }
    }

    fun editPlaylistTitle(oldName: String, newName: String) {
        viewModelScope.launch {
            libraryRepository.updatePlaylistName(oldName, newName)
            _playlistDetailState.update {
                it.copy(playlistName = newName)
            }
        }
    }


    fun popBack() {
        popBackStack()
    }

    fun onArtistClicked(artistId: String, artistName: String) {
        if (artistId.isNotBlank()) {
            navigate(Route.ArtistProfile(artistId, artistName))
        }
    }

    fun onRepostPressed(id: String, isRepost: Boolean) {
        if (authRepository.sessionState.value is com.rld.justlisten.datalayer.repositories.SessionState.Guest) {
            _playlistDetailState.update { it.copy(showConnectPrompt = true) }
            return
        }

        viewModelScope.launch {
            val success = if (isRepost) {
                playlistRepository.repostTrack(id)
            } else {
                playlistRepository.unrepostTrack(id)
            }
            if (success) {
                _playlistDetailState.update { state ->
                    val updated = state.songPlaylist.map { item ->
                        if (item.id == id) {
                            val newCount = if (isRepost) item.repostCount + 1 else (item.repostCount - 1).coerceAtLeast(0)
                            val updatedData = item._data.copy(repostCount = newCount)
                            item.copy(_data = updatedData, isReposted = isRepost)
                        } else item
                    }
                    state.copy(songPlaylist = updated)
                }
            }
        }
    }

    fun dismissConnectPrompt() {
        _playlistDetailState.update { it.copy(showConnectPrompt = false) }
    }
}
