package com.rld.justlisten.viewmodel.player

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.ui.actions.PlayerAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

import com.rld.justlisten.media.PlayHistoryTracker

import com.rld.justlisten.datalayer.repositories.AuthRepository

import com.rld.justlisten.datalayer.repositories.PlaylistRepository

import com.rld.justlisten.datalayer.repositories.FeedRepository
import com.rld.justlisten.datalayer.repositories.SettingsRepository
import com.rld.justlisten.datalayer.repositories.SessionState
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import kotlinx.coroutines.IO

class PlayerViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val libraryRepository: LibraryRepository,
    private val playlistRepository: PlaylistRepository,
    private val musicPlayer: MusicPlayer,
    private val playHistoryTracker: PlayHistoryTracker,
    private val authRepository: AuthRepository,
    private val feedRepository: FeedRepository,
    private val settingsRepository: SettingsRepository,
) : BaseScreenViewModel() {

    private var fetchDetailsJob: kotlinx.coroutines.Job? = null
    private val _recommendedSongs = MutableStateFlow<List<PlaylistItem>>(emptyList())
    private val _isAutoplayEnabled = MutableStateFlow(true)
    private var recommendedOffset = 0
    private val autoplayedTrackIds = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.getSettingsInfo()
            _isAutoplayEnabled.value = settings.isOngoingStreamEnabled
        }

        viewModelScope.launch {
            var lastTrackId: String? = null
            var lastPlayedTrackId: String? = null
            musicPlayer.playbackState.collect { state ->
                val media = state.currentMedia
                val trackId = media?.id
                if (trackId != null) {
                    lastPlayedTrackId = trackId
                    if (trackId != lastTrackId) {
                        lastTrackId = trackId
                        fetchDetailsJob?.cancel()
                        fetchDetailsJob = viewModelScope.launch {
                            try {
                                val details = playlistRepository.fetchTrackDetails(trackId)
                                if (details != null) {
                                    musicPlayer.updateTrackMetadata(
                                        songId = trackId,
                                        repostCount = details.repostCount,
                                        favoriteCount = details.favoriteCount,
                                        commentCount = details.commentCount,
                                        playCount = details.playCount,
                                        artistId = details.user.id
                                    )
                                }
                            } catch (e: Exception) {
                                // Ignore API fetch errors (e.g. offline)
                            }
                        }
                        if (!autoplayedTrackIds.contains(trackId)) {
                            recommendedOffset = 0
                            autoplayedTrackIds.clear()
                        }
                        fetchRecommendations(trackId)
                    }
                }

                if (state.status == PlaybackStatus.STOPPED) {
                    val playlist = musicPlayer.currentPlaylist.value
                    val lastTrackInPlaylist = playlist.lastOrNull()
                    if (lastTrackInPlaylist != null && lastTrackInPlaylist.id == lastPlayedTrackId) {
                        val settingsInfo = settingsRepository.getSettingsInfo()
                        if (settingsInfo.isOngoingStreamEnabled) {
                            playNextAutoplaySong()
                        }
                    }
                }
            }
        }
    }

    private fun fetchRecommendations(currentTrackId: String) {
        viewModelScope.launch {
            try {
                val session = authRepository.sessionState.value
                val currentPlaylistIds = musicPlayer.currentPlaylist.value.map { it.id }.toSet()
                
                val recommendedTracks = if (session is SessionState.Authenticated) {
                    feedRepository.getUserFeed(
                        userId = session.userProfile.userId ?: "",
                        limit = 30,
                        offset = recommendedOffset,
                        tracksOnly = true
                    )
                } else {
                    playlistRepository.getTracks(
                        limit = 100,
                        category = "",
                        timeRange = "week"
                    ).map { PlaylistItem(it._data, it.isFavorite, it.isReposted) }
                     .drop(recommendedOffset % 100)
                }
                
                var filteredTracks = recommendedTracks.filter { 
                    it.id != currentTrackId && !currentPlaylistIds.contains(it.id) 
                }.take(10)
                
                if (filteredTracks.isEmpty() && recommendedOffset > 0) {
                    recommendedOffset = 0
                    val retryTracks = if (session is SessionState.Authenticated) {
                        feedRepository.getUserFeed(
                            userId = session.userProfile.userId ?: "",
                            limit = 30,
                            offset = 0,
                            tracksOnly = true
                        )
                    } else {
                        playlistRepository.getTracks(
                            limit = 100,
                            category = "",
                            timeRange = "week"
                        ).map { PlaylistItem(it._data, it.isFavorite, it.isReposted) }
                    }
                    filteredTracks = retryTracks.filter { 
                        it.id != currentTrackId && !currentPlaylistIds.contains(it.id) 
                    }.take(10)
                }
                
                _recommendedSongs.value = filteredTracks
            } catch (e: Exception) {
                // Ignore or log error
            }
        }
    }

    private fun playNextAutoplaySong() {
        val tracks = _recommendedSongs.value
        if (tracks.isNotEmpty()) {
            autoplayedTrackIds.addAll(tracks.map { it.id })
            musicPlayer.addTracksToQueue(tracks)
            musicPlayer.playMedia(tracks.first().id)
            _recommendedSongs.value = emptyList()
            recommendedOffset += 10
            fetchRecommendations(tracks.first().id)
        }
    }

    private val _addPlaylistList = MutableStateFlow(emptyList<AddPlaylist>())
    val addPlaylistList: StateFlow<List<AddPlaylist>> = _addPlaylistList.asStateFlow()

    private val _showConnectPrompt = MutableStateFlow(false)

    val playerUiState: StateFlow<PlayerUiState> = combine(
        _addPlaylistList,
        musicPlayer.playbackState.map { state ->
            state.copy(currentPosition = 0)
        }.distinctUntilChanged(),
        _showConnectPrompt,
        _isAutoplayEnabled,
        _recommendedSongs
    ) { addPlaylists, playbackState, showConnectPrompt, autoplayEnabled, recommended ->
        PlayerUiState(
            addPlaylistList = addPlaylists,
            playbackState = playbackState,
            showConnectPrompt = showConnectPrompt,
            isAutoplayEnabled = autoplayEnabled,
            recommendedSongs = recommended
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState()
    )

    fun onAction(action: PlayerAction) {
        when (action) {
            is PlayerAction.ToggleFavorite -> {
                saveSongToFavorites(
                    action.songId,
                    action.title,
                    action.user,
                    action.songIcon,
                    action.isFavorite
                )
            }
            is PlayerAction.ToggleRepost -> {
                toggleRepost(action.songId, action.isRepost)
            }
            PlayerAction.DismissConnectPrompt -> {
                _showConnectPrompt.value = false
            }
            is PlayerAction.CreatePlaylist -> {
                savePlaylist(action.name, action.description)
            }
            is PlayerAction.AddSongToPlaylist -> {
                updatePlaylistSongs(action.playlistTitle, action.playlistDescription, action.songs)
            }
            PlayerAction.LoadPlaylists -> {
                loadAddPlaylists()
            }
            PlayerAction.SkipNext -> {
                val playlist = musicPlayer.currentPlaylist.value
                val currentMedia = musicPlayer.playbackState.value.currentMedia
                val currentIndex = playlist.indexOfFirst { it.id == currentMedia?.id }
                val settings = settingsRepository.getSettingsInfo()
                if (settings.isOngoingStreamEnabled && currentIndex == playlist.size - 1) {
                    playNextAutoplaySong()
                } else {
                    musicPlayer.skipToNext()
                }
            }
            PlayerAction.SkipPrevious -> {
                musicPlayer.skipToPrevious()
            }
            is PlayerAction.ToggleAutoplay -> {
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    val settings = settingsRepository.getSettingsInfo()
                    settingsRepository.saveSettingsInfo(
                        hasNavigationDonationOn = settings.hasNavigationDonationOn,
                        isDarkThemeOn = settings.isDarkThemeOn,
                        palletColor = settings.palletColor,
                        customPrimary = settings.customPrimary,
                        customSecondary = settings.customSecondary,
                        customBackground = settings.customBackground,
                        customSurface = settings.customSurface,
                        isFirstLaunch = settings.isFirstLaunch,
                        isOngoingStreamEnabled = action.enabled
                    )
                    _isAutoplayEnabled.value = action.enabled
                }
            }
            is PlayerAction.PlayRecommendedTrack -> {
                val tracks = _recommendedSongs.value
                val index = tracks.indexOfFirst { it.id == action.songId }
                if (index != -1) {
                    musicPlayer.updatePlaylist(tracks)
                    _recommendedSongs.value = emptyList()
                    autoplayedTrackIds.clear()
                    autoplayedTrackIds.addAll(tracks.map { it.id })
                    recommendedOffset += 10
                    musicPlayer.playMedia(action.songId)
                    fetchRecommendations(action.songId)
                }
            }
            else -> Unit
        }
    }

    fun loadAddPlaylists() {
        viewModelScope.launch {
            _addPlaylistList.value = libraryRepository.getAddPlaylist()
        }
    }

    fun saveSongToFavorites(
        id: String, title: String, user: UserModel, songIcon: SongIconList, isFavorite: Boolean
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
            loadAddPlaylists()
        }
    }

    fun toggleRepost(songId: String, isRepost: Boolean) {
        if (authRepository.sessionState.value is com.rld.justlisten.datalayer.repositories.SessionState.Guest) {
            _showConnectPrompt.value = true
            return
        }
        viewModelScope.launch {
            val success = if (isRepost) {
                playlistRepository.repostTrack(songId)
            } else {
                playlistRepository.unrepostTrack(songId)
            }
            if (success) {
                musicPlayer.refreshMetadata()
            }
        }
    }
}
