package com.rld.justlisten.viewmodel.library

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.library.LibraryState
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val libraryRepository: LibraryRepository,
    private val favoritesRepository: FavoritesRepository,
) : BaseScreenViewModel() {

    private val _libraryState = MutableStateFlow(LibraryState(isLoading = true))
    val libraryState: StateFlow<LibraryState> = _libraryState.asStateFlow()

    init {
        loadLibraryData()
        viewModelScope.launch {
            favoritesRepository.getFavoritePlaylistFlow().collect {
                loadLibraryData()
            }
        }
    }

    private fun loadLibraryData() {
        viewModelScope.launch {
            try {
                val recent = libraryRepository.getRecentSongs(20).map { PlaylistItem(it, it.isFavorite) }
                val favorites = favoritesRepository.getFavoritePlaylist().map { PlaylistItem(it, it.isFavorite) }
                val mostPlayed = libraryRepository.getMostPlayedSongs(20).map { PlaylistItem(it, it.isFavorite) }
                val playlistsCreated = libraryRepository.getAddPlaylist()
                val allPlayed = libraryRepository.getMostPlayedSongs(1000)
                val totalPlays = allPlayed.sumOf { it.songCounter.toIntOrNull() ?: 0 }
                val topArtistGroup = allPlayed
                    .groupBy { it.user.username }
                    .mapValues { entry -> entry.value.sumOf { it.songCounter.toIntOrNull() ?: 0 } }
                    .maxByOrNull { it.value }
                val topArtistName = topArtistGroup?.key ?: ""
                val topArtistPlays = topArtistGroup?.value ?: 0
                val timeCapsule = libraryRepository.getTimeCapsuleSongs(20).map { PlaylistItem(it, it.isFavorite) }
                
                _libraryState.update {
                    it.copy(
                        isLoading = false,
                        recentSongsItems = recent,
                        favoritePlaylistItems = favorites,
                        mostPlayedSongs = mostPlayed,
                        playlistsCreated = playlistsCreated,
                        timeCapsuleSongs = timeCapsule,
                        totalPlays = totalPlays,
                        topArtistName = topArtistName,
                        topArtistPlays = topArtistPlays
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

    fun onTimeCapsuleClicked() {
        navigate(
            Route.PlaylistDetail(
                playlistId = "TimeCapsule",
                playlistIcon = "",
                playlistTitle = "Time Capsule",
                playlistCreatedBy = "You",
                playlistEnum = "TIME_CAPSULE",
            ),
        )
    }

    fun onExploreMusicClicked() {
        navigate(Route.Playlist)
    }

    fun onMusicInsightsClicked() {
        navigate(Route.MusicInsights)
    }

    fun popBack() {
        popBackStack()
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
            libraryRepository.deletePlaylist(playlistName)
            loadLibraryData()
        }
    }

    fun loadMoreRecentSongs(currentCount: Int) {
        viewModelScope.launch {
            val recent = libraryRepository.getRecentSongs((currentCount + 20).toLong())
                .map { PlaylistItem(it, it.isFavorite) }
            if (recent.size == _libraryState.value.recentSongsItems.size) {
                _libraryState.update { it.copy(lastIndexReached = true) }
            } else {
                _libraryState.update { it.copy(recentSongsItems = recent) }
            }
        }
    }
}
