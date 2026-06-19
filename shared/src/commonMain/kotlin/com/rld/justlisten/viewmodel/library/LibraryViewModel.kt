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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class LibraryViewModel(
    private val libraryRepository: LibraryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val authRepository: com.rld.justlisten.datalayer.repositories.AuthRepository,
    private val syncRepository: com.rld.justlisten.datalayer.repositories.SyncRepository
) : BaseScreenViewModel() {

    private val _libraryState = MutableStateFlow(LibraryState(isLoading = true))
    val libraryState: StateFlow<LibraryState> = _libraryState.asStateFlow()

    init {
        loadLibraryData()
        
        viewModelScope.launch {
            authRepository.sessionState.collect { session ->
                _libraryState.update { it.copy(sessionState = session) }
            }
        }
        
        viewModelScope.launch {
            favoritesRepository.getFavoritePlaylistFlow().collect { favoriteList ->
                val mapped = favoriteList.map { PlaylistItem(it, it.isFavorite) }
                _libraryState.update { it.copy(favoritePlaylistItems = mapped) }
            }
        }
        viewModelScope.launch {
            libraryRepository.getAddPlaylistFlow().collect { playlistList ->
                _libraryState.update { it.copy(playlistsCreated = playlistList) }
            }
        }
        var debounceJob: kotlinx.coroutines.Job? = null
        viewModelScope.launch {
            libraryRepository.getPlayHistoryFlow().collect {
                debounceJob?.cancel()
                debounceJob = launch {
                    kotlinx.coroutines.delay(2000L)
                    loadPlayHistoryAndStats()
                }
            }
        }
    }

    private fun loadPlayHistoryAndStats() {
        viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {

                    val recent = libraryRepository.getRecentSongs(20).map { PlaylistItem(it, it.isFavorite) }
                    val mostPlayed = libraryRepository.getMostPlayedSongsFromHistory(20, 0)
                        .map { PlaylistItem(it, it.isFavorite) }
                    val totalPlays = libraryRepository.getTotalPlays().toInt()
                    val uniquePlays = libraryRepository.getUniquePlays().toInt()
                    val totalDurationSeconds = libraryRepository.getTotalDurationPlayed()
                    val hoursPlayed = ((totalDurationSeconds / 3600.0) * 10).toInt() / 10.0
                    val topArtistGroup = libraryRepository.getTopArtistFromHistory()
                    val topArtistName = topArtistGroup?.first?.username ?: ""
                    val topArtistPlays = topArtistGroup?.second?.toInt() ?: 0
                    val topArtistDurationSec = topArtistGroup?.third ?: 0L
                    val topArtistHours = ((topArtistDurationSec / 3600.0) * 10).toInt() / 10.0
                    val timeCapsule = libraryRepository.getTimeCapsuleSongs(20).map { PlaylistItem(it, it.isFavorite) }
                    
                    PlayHistoryData(
                        recent = recent,
                        mostPlayed = mostPlayed,
                        totalPlays = totalPlays,
                        uniquePlays = uniquePlays,
                        hoursPlayed = hoursPlayed,
                        topArtistName = topArtistName,
                        topArtistPlays = topArtistPlays,
                        topArtistHours = topArtistHours,
                        timeCapsule = timeCapsule
                    )
                }
                
                _libraryState.update {
                    it.copy(
                        recentSongsItems = data.recent,
                        mostPlayedSongs = data.mostPlayed,
                        totalPlays = data.totalPlays,
                        uniquePlays = data.uniquePlays,
                        hoursPlayed = data.hoursPlayed,
                        topArtistName = data.topArtistName,
                        topArtistPlays = data.topArtistPlays,
                        topArtistHours = data.topArtistHours,
                        timeCapsuleSongs = data.timeCapsule,
                        lastMostPlayedIndexReached = data.mostPlayed.size < 20
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadLibraryData() {
        viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    val recent = libraryRepository.getRecentSongs(20).map { PlaylistItem(it, it.isFavorite) }
                    val favorites = favoritesRepository.getFavoritePlaylist().map { PlaylistItem(it, it.isFavorite) }
                    val mostPlayed = libraryRepository.getMostPlayedSongsFromHistory(20, 0)
                        .map { PlaylistItem(it, it.isFavorite) }
                    val playlistsCreated = libraryRepository.getAddPlaylist()
                    val totalPlays = libraryRepository.getTotalPlays().toInt()
                    val uniquePlays = libraryRepository.getUniquePlays().toInt()
                    val totalDurationSeconds = libraryRepository.getTotalDurationPlayed()
                    val hoursPlayed = ((totalDurationSeconds / 3600.0) * 10).toInt() / 10.0
                    val topArtistGroup = libraryRepository.getTopArtistFromHistory()
                    val topArtistName = topArtistGroup?.first?.username ?: ""
                    val topArtistPlays = topArtistGroup?.second?.toInt() ?: 0
                    val topArtistDurationSec = topArtistGroup?.third ?: 0L
                    val topArtistHours = ((topArtistDurationSec / 3600.0) * 10).toInt() / 10.0
                    val timeCapsule = libraryRepository.getTimeCapsuleSongs(20).map { PlaylistItem(it, it.isFavorite) }

                    LibraryData(
                        recent = recent,
                        favorites = favorites,
                        mostPlayed = mostPlayed,
                        playlistsCreated = playlistsCreated,
                        totalPlays = totalPlays,
                        uniquePlays = uniquePlays,
                        hoursPlayed = hoursPlayed,
                        topArtistName = topArtistName,
                        topArtistPlays = topArtistPlays,
                        topArtistHours = topArtistHours,
                        timeCapsule = timeCapsule
                    )
                }

                _libraryState.update {
                    it.copy(
                        isLoading = false,
                        recentSongsItems = data.recent,
                        favoritePlaylistItems = data.favorites,
                        mostPlayedSongs = data.mostPlayed,
                        playlistsCreated = data.playlistsCreated,
                        timeCapsuleSongs = data.timeCapsule,
                        totalPlays = data.totalPlays,
                        uniquePlays = data.uniquePlays,
                        hoursPlayed = data.hoursPlayed,
                        topArtistName = data.topArtistName,
                        topArtistPlays = data.topArtistPlays,
                        topArtistHours = data.topArtistHours,
                        lastMostPlayedIndexReached = data.mostPlayed.size < 20
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

    fun onArtistDashboardClicked() {
        navigate(Route.ArtistDashboard)
    }

    fun popBack() {
        popBackStack()
    }

    fun onAddPlaylistClicked() {
        navigate(Route.AddPlaylist())
    }

    fun onPlaylistCreatedClicked(
        title: String, 
        description: String?, 
        songs: List<String>, 
        isRemote: Boolean = false, 
        isPrivate: Boolean = false
    ) {
        viewModelScope.launch {
            val exists = _libraryState.value.playlistsCreated.any { it.playlistName == title }
            if (!exists) {
                libraryRepository.savePlaylist(title, description, isRemote, isPrivate)
                if (isRemote) {
                    syncRepository.enqueuePlaylistCreateTask(title, description, isPrivate)
                }
            }
            navigate(
                Route.PlaylistDetail(
                    playlistId = "",
                    playlistIcon = "",
                    playlistTitle = title,
                    playlistCreatedBy = if (isRemote) "ME (Audius)" else "ME",
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

    fun onArtistClicked(artistId: String, artistName: String) {
        if (artistId.isNotBlank()) {
            navigate(Route.ArtistProfile(artistId, artistName))
        }
    }
}

private data class LibraryData(
    val recent: List<PlaylistItem>,
    val favorites: List<PlaylistItem>,
    val mostPlayed: List<PlaylistItem>,
    val playlistsCreated: List<com.rld.justlisten.database.addplaylistscreen.AddPlaylist>,
    val totalPlays: Int,
    val uniquePlays: Int,
    val hoursPlayed: Double,
    val topArtistName: String,
    val topArtistPlays: Int,
    val topArtistHours: Double,
    val timeCapsule: List<PlaylistItem>
)

private data class PlayHistoryData(
    val recent: List<PlaylistItem>,
    val mostPlayed: List<PlaylistItem>,
    val totalPlays: Int,
    val uniquePlays: Int,
    val hoursPlayed: Double,
    val topArtistName: String,
    val topArtistPlays: Int,
    val topArtistHours: Double,
    val timeCapsule: List<PlaylistItem>
)
