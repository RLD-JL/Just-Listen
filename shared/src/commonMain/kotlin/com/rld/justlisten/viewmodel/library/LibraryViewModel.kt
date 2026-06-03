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
        viewModelScope.launch {
            libraryRepository.getAddPlaylistFlow().collect {
                loadLibraryData()
            }
        }
        viewModelScope.launch {
            libraryRepository.getPlayHistoryFlow().collect {
                loadLibraryData()
            }
        }
    }

    private fun loadLibraryData() {
        viewModelScope.launch {
            try {
                val recent = libraryRepository.getRecentSongs(20).map { PlaylistItem(it, it.isFavorite) }
                val favorites = favoritesRepository.getFavoritePlaylist().map { PlaylistItem(it, it.isFavorite) }
                
                // Fetch top played songs from history
                val mostPlayed = libraryRepository.getMostPlayedSongsFromHistory(20, 0)
                    .map { PlaylistItem(it, it.isFavorite) }
                
                val playlistsCreated = libraryRepository.getAddPlaylist()
                
                // Fetch total play and unique plays count directly
                val totalPlays = libraryRepository.getTotalPlays().toInt()
                val uniquePlays = libraryRepository.getUniquePlays().toInt()
                
                // Fetch total duration and calculate hours
                val totalDurationSeconds = libraryRepository.getTotalDurationPlayed()
                val hoursPlayed = ((totalDurationSeconds / 3600.0) * 10).toInt() / 10.0
                
                // Fetch top artist from history
                val topArtistGroup = libraryRepository.getTopArtistFromHistory()
                val topArtistName = topArtistGroup?.first?.username ?: ""
                val topArtistPlays = topArtistGroup?.second?.toInt() ?: 0
                val topArtistDurationSec = topArtistGroup?.third ?: 0L
                val topArtistHours = ((topArtistDurationSec / 3600.0) * 10).toInt() / 10.0
                
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
                        uniquePlays = uniquePlays,
                        hoursPlayed = hoursPlayed,
                        topArtistName = topArtistName,
                        topArtistPlays = topArtistPlays,
                        topArtistHours = topArtistHours,
                        lastMostPlayedIndexReached = mostPlayed.size < 20
                    )
                }
            } catch (e: Exception) {
                _libraryState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadMoreMostPlayedSongs(currentCount: Int) {
        viewModelScope.launch {
            try {
                val morePlayed = libraryRepository.getMostPlayedSongsFromHistory(20, currentCount.toLong())
                    .map { PlaylistItem(it, it.isFavorite) }
                if (morePlayed.isEmpty()) {
                    _libraryState.update { it.copy(lastMostPlayedIndexReached = true) }
                } else {
                    _libraryState.update { state ->
                        val combined = (state.mostPlayedSongs + morePlayed).distinctBy { it.id }
                        state.copy(
                            mostPlayedSongs = combined,
                            lastMostPlayedIndexReached = morePlayed.size < 20
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore pagination load errors
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
        viewModelScope.launch {
            val exists = _libraryState.value.playlistsCreated.any { it.playlistName == title }
            if (!exists) {
                libraryRepository.savePlaylist(title, description)
            }
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
