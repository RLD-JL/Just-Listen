package com.rld.justlisten.viewmodel.library

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.datacalls.library.getFavoritePlaylist
import com.rld.justlisten.datalayer.datacalls.library.getMostPlayedSongs
import com.rld.justlisten.datalayer.datacalls.library.getRecentSongs
import com.rld.justlisten.datalayer.datacalls.addplaylistscreen.getAddPlaylist
import com.rld.justlisten.datalayer.datacalls.addplaylistscreen.deletePlaylist
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.library.LibraryState
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: Repository,
) : BaseScreenViewModel() {

    private val _libraryState = MutableStateFlow(LibraryState(isLoading = true))
    val libraryState: StateFlow<LibraryState> = _libraryState.asStateFlow()

    init {
        loadLibraryData()
        viewModelScope.launch {
            repository.getFavoritePlaylistFlow().collect {
                loadLibraryData()
            }
        }
    }

    private fun loadLibraryData() {
        viewModelScope.launch {
            try {
                val recent = repository.getRecentSongs(20).map { PlaylistItem(it, it.isFavorite) }
                val favorites = repository.getFavoritePlaylist().map { PlaylistItem(it, it.isFavorite) }
                val mostPlayed = repository.getMostPlayedSongs(20).map { PlaylistItem(it, it.isFavorite) }
                val playlistsCreated = repository.getAddPlaylist()
                _libraryState.update {
                    it.copy(
                        isLoading = false,
                        recentSongsItems = recent,
                        favoritePlaylistItems = favorites,
                        mostPlayedSongs = mostPlayed,
                        playlistsCreated = playlistsCreated,
                    )
                }
            } catch (e: Exception) {
                _libraryState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onFavoritePlaylistClicked(
        playlistId: String,
        playlistIcon: String,
        playlistTitle: String,
        playlistCreatedBy: String,
    ) {
        navigate(
            Route.PlaylistDetail(
                playlistId = playlistId,
                playlistIcon = playlistIcon,
                playlistTitle = playlistTitle,
                playlistCreatedBy = playlistCreatedBy,
                playlistEnum = "FAVORITE",
            ),
        )
    }

    fun onMostPlayedPlaylistClicked(
        playlistId: String,
        playlistIcon: String,
        playlistTitle: String,
        playlistCreatedBy: String,
    ) {
        navigate(
            Route.PlaylistDetail(
                playlistId = playlistId,
                playlistIcon = playlistIcon,
                playlistTitle = playlistTitle,
                playlistCreatedBy = playlistCreatedBy,
                playlistEnum = "MOST_PLAYED",
            ),
        )
    }

    fun onAddPlaylistClicked() {
        navigate(Route.AddPlaylist())
    }

    fun onPlaylistCreatedClicked(title: String, description: String?, songs: List<String>) {
        navigate(
            Route.PlaylistDetail(
                playlistId = "",
                playlistIcon = "",
                playlistTitle = title,
                playlistCreatedBy = "ME",
                playlistEnum = "CREATED_BY_USER",
                songsList = songs
            ),
        )
    }

    fun deletePlaylist(playlistName: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistName)
            loadLibraryData()
        }
    }

    fun loadMoreRecentSongs(currentCount: Int) {
        viewModelScope.launch {
            val recent = repository.getRecentSongs((currentCount + 20).toLong())
                .map { PlaylistItem(it, it.isFavorite) }
            if (recent.size == _libraryState.value.recentSongsItems.size) {
                _libraryState.update { it.copy(lastIndexReached = true) }
            } else {
                _libraryState.update { it.copy(recentSongsItems = recent) }
            }
        }
    }
}
