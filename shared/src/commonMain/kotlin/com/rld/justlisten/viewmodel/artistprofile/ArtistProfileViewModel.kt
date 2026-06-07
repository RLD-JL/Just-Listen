package com.rld.justlisten.viewmodel.artistprofile

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserProfile
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserPlaylists
import com.rld.justlisten.datalayer.webservices.apis.writecalls.followUser
import com.rld.justlisten.datalayer.webservices.apis.writecalls.unfollowUser
import com.rld.justlisten.datalayer.repositories.AuthRepository
import com.rld.justlisten.datalayer.repositories.SessionState
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.artistprofile.ArtistProfileState
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ArtistProfileViewModel(
    private val apiClient: ApiClient,
    private val authRepository: AuthRepository,
    private val favoritesRepository: FavoritesRepository
) : BaseScreenViewModel() {

    private val _artistProfileState = MutableStateFlow(ArtistProfileState())
    val artistProfileState: StateFlow<ArtistProfileState> = _artistProfileState.asStateFlow()

    private var loadJob: kotlinx.coroutines.Job? = null

    fun load(args: Route.ArtistProfile) {
        val artistId = args.artistId
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _artistProfileState.update { 
                it.copy(
                    isLoading = true, 
                    artistProfile = null, 
                    artistTracks = emptyList(), 
                    artistPlaylists = emptyList(),
                    showConnectPrompt = false
                ) 
            }
            try {
                // Fetch profile, tracks, and playlists concurrently
                val profileDeferred = async { apiClient.getUserProfile(artistId) }
                val tracksDeferred = async { 
                    apiClient.getResponse<com.rld.justlisten.datalayer.webservices.apis.playlistcalls.PlayListResponse>("/users/$artistId/tracks") 
                }
                val playlistsDeferred = async { apiClient.getUserPlaylists(artistId) }

                val profile = profileDeferred.await()
                val tracksResponse = tracksDeferred.await()
                val playlists = playlistsDeferred.await()

                val favoriteIds = favoritesRepository.getFavoritePlaylist().map { it.id }.toSet()
                val playlistItems = tracksResponse?.data?.map { track ->
                    PlaylistItem(track, favoriteIds.contains(track.id))
                } ?: emptyList()

                _artistProfileState.update {
                    it.copy(
                        isLoading = false,
                        artistProfile = profile,
                        artistTracks = playlistItems,
                        artistPlaylists = playlists
                    )
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    println("ArtistProfileViewModel: Error loading profile: ${e.message}")
                    _artistProfileState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onTabSelected(index: Int) {
        _artistProfileState.update { it.copy(selectedTabIndex = index) }
    }

    fun onFollowPressed() {
        val session = authRepository.sessionState.value
        if (session !is SessionState.Authenticated) {
            _artistProfileState.update { it.copy(showConnectPrompt = true) }
            return
        }

        val profile = _artistProfileState.value.artistProfile ?: return
        val currentlyFollowing = profile.doesCurrentUserFollow
        val artistId = profile.id

        viewModelScope.launch {
            val response = if (currentlyFollowing) {
                apiClient.unfollowUser(artistId)
            } else {
                apiClient.followUser(artistId)
            }

            if (response?.error == null) {
                // Update in-memory state
                val updatedProfile = profile.copy(
                    doesCurrentUserFollow = !currentlyFollowing,
                    followerCount = if (currentlyFollowing) profile.followerCount - 1 else profile.followerCount + 1
                )
                _artistProfileState.update {
                    it.copy(artistProfile = updatedProfile)
                }
            }
        }
    }

    fun dismissConnectPrompt() {
        _artistProfileState.update { it.copy(showConnectPrompt = false) }
    }

    fun onPlaylistClicked(playlistId: String, playlistIcon: String, createdBy: String, title: String) {
        navigate(
            Route.PlaylistDetail(
                playlistId = playlistId,
                playlistIcon = playlistIcon,
                playlistTitle = title,
                playlistCreatedBy = createdBy,
                playlistEnum = "CREATED_BY_USER"
            )
        )
    }

    fun popBack() {
        popBackStack()
    }
}
