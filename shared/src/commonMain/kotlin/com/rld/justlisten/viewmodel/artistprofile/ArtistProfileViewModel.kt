package com.rld.justlisten.viewmodel.artistprofile

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserProfile
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserPlaylists
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserFollowers
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserFollowing
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserCoins
import com.rld.justlisten.datalayer.webservices.apis.authcalls.UserCoinModel
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
import co.touchlab.kermit.Logger

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
                    showConnectPrompt = false,
                    isCurrentUser = false
                ) 
            }
            try {
                val session = authRepository.sessionState.value
                val currentUserId = if (session is SessionState.Authenticated) session.userProfile.userId else null
                val isCurrentUser = currentUserId != null && currentUserId == artistId

                // Fetch profile, tracks, and playlists concurrently
                val profileDeferred = async { apiClient.getUserProfile(artistId) }
                val tracksDeferred = async { 
                    apiClient.getResponse<com.rld.justlisten.datalayer.webservices.apis.playlistcalls.PlayListResponse>("/users/$artistId/tracks") 
                }
                val playlistsDeferred = async { apiClient.getUserPlaylists(artistId) }
                val coinsDeferred = async { apiClient.getUserCoins(artistId) }

                var profile = profileDeferred.await()
                val coins = coinsDeferred.await()

                if (profile != null && isCurrentUser) {
                    val customName = authRepository.getCustomName(artistId)
                    val customBio = authRepository.getCustomBio(artistId)
                    val customProfilePic = authRepository.getCustomProfilePic(artistId)
                    val customCoverPhoto = authRepository.getCustomCoverPhoto(artistId)
                    val customLocation = authRepository.getCustomLocation(artistId)
                    val customXHandle = authRepository.getCustomXHandle(artistId)
                    val customInstagramHandle = authRepository.getCustomInstagramHandle(artistId)
                    val customTikTokHandle = authRepository.getCustomTikTokHandle(artistId)
                    val customWebsite = authRepository.getCustomWebsite(artistId)
                    val customFanClubFlair = authRepository.getCustomFanClubFlair(artistId)

                    val resolvedFlairMint = when (customFanClubFlair) {
                        "Highest Balance" -> {
                            coins.maxByOrNull { it.balanceUsd ?: it.balance.toDoubleOrNull() ?: 0.0 }?.mint
                        }
                        "None" -> null
                        null -> profile.coinFlairMint
                        else -> customFanClubFlair
                    }

                    profile = profile.copy(
                        name = customName ?: profile.name,
                        bio = customBio ?: profile.bio,
                        profilePicture = if (!customProfilePic.isNullOrBlank()) {
                            com.rld.justlisten.datalayer.webservices.apis.authcalls.ProfileImages(
                                image150 = customProfilePic,
                                image480 = customProfilePic,
                                image1000 = customProfilePic
                            )
                        } else profile.profilePicture,
                        coverPhoto = if (!customCoverPhoto.isNullOrBlank()) {
                            com.rld.justlisten.datalayer.webservices.apis.authcalls.ProfileImages(
                                image640 = customCoverPhoto,
                                image2000 = customCoverPhoto
                            )
                        } else profile.coverPhoto,
                        location = customLocation ?: profile.location,
                        twitterHandle = customXHandle ?: profile.twitterHandle,
                        instagramHandle = customInstagramHandle ?: profile.instagramHandle,
                        tiktokHandle = customTikTokHandle ?: profile.tiktokHandle,
                        website = customWebsite ?: profile.website,
                        coinFlairMint = resolvedFlairMint,
                        fanClubFlair = customFanClubFlair ?: profile.fanClubFlair
                    )
                }

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
                        artistPlaylists = playlists,
                        isCurrentUser = isCurrentUser,
                        userCoins = coins
                    )
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Logger.e(e) { "ArtistProfileViewModel: Error loading profile" }
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

    fun onEditProfileSaved(
        name: String,
        bio: String?,
        profilePicUrl: String?,
        coverPhotoUrl: String?,
        location: String?,
        xHandle: String?,
        instagramHandle: String?,
        tiktokHandle: String?,
        website: String?,
        fanClubFlair: String?
    ) {
        val profile = _artistProfileState.value.artistProfile ?: return
        val artistId = profile.id
        authRepository.updateUserProfile(
            artistId, name, bio, profilePicUrl, coverPhotoUrl,
            location, xHandle, instagramHandle, tiktokHandle, website, fanClubFlair
        )
        
        val coins = _artistProfileState.value.userCoins
        val resolvedFlairMint = when (fanClubFlair) {
            "Highest Balance" -> {
                coins.maxByOrNull { it.balanceUsd ?: it.balance.toDoubleOrNull() ?: 0.0 }?.mint
            }
            "None" -> null
            null -> profile.coinFlairMint
            else -> fanClubFlair
        }

        val updatedProfile = profile.copy(
            name = name,
            bio = bio ?: profile.bio,
            profilePicture = if (!profilePicUrl.isNullOrBlank()) {
                com.rld.justlisten.datalayer.webservices.apis.authcalls.ProfileImages(
                    image150 = profilePicUrl,
                    image480 = profilePicUrl,
                    image1000 = profilePicUrl
                )
            } else profile.profilePicture,
            coverPhoto = if (!coverPhotoUrl.isNullOrBlank()) {
                com.rld.justlisten.datalayer.webservices.apis.authcalls.ProfileImages(
                    image640 = coverPhotoUrl,
                    image2000 = coverPhotoUrl
                )
            } else profile.coverPhoto,
            location = location ?: profile.location,
            twitterHandle = xHandle ?: profile.twitterHandle,
            instagramHandle = instagramHandle ?: profile.instagramHandle,
            tiktokHandle = tiktokHandle ?: profile.tiktokHandle,
            website = website ?: profile.website,
            coinFlairMint = resolvedFlairMint,
            fanClubFlair = fanClubFlair ?: profile.fanClubFlair
        )
        _artistProfileState.update {
            it.copy(artistProfile = updatedProfile)
        }
    }

    fun onFollowersClicked() {
        val profile = _artistProfileState.value.artistProfile ?: return
        val artistId = profile.id
        _artistProfileState.update {
            it.copy(
                showSocialSheet = true,
                socialSheetTitle = "Followers",
                isSocialLoading = true,
                socialUsersList = emptyList()
            )
        }
        viewModelScope.launch {
            try {
                val followers = apiClient.getUserFollowers(artistId)
                _artistProfileState.update {
                    it.copy(
                        isSocialLoading = false,
                        socialUsersList = followers
                    )
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Logger.e(e) { "ArtistProfileViewModel: Error fetching followers" }
                    _artistProfileState.update { it.copy(isSocialLoading = false) }
                }
            }
        }
    }

    fun onFollowingClicked() {
        val profile = _artistProfileState.value.artistProfile ?: return
        val artistId = profile.id
        _artistProfileState.update {
            it.copy(
                showSocialSheet = true,
                socialSheetTitle = "Following",
                isSocialLoading = true,
                socialUsersList = emptyList()
            )
        }
        viewModelScope.launch {
            try {
                val following = apiClient.getUserFollowing(artistId)
                _artistProfileState.update {
                    it.copy(
                        isSocialLoading = false,
                        socialUsersList = following
                    )
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Logger.e(e) { "ArtistProfileViewModel: Error fetching following" }
                    _artistProfileState.update { it.copy(isSocialLoading = false) }
                }
            }
        }
    }

    fun onDismissSocialSheet() {
        _artistProfileState.update { it.copy(showSocialSheet = false) }
    }

    fun onNavigateToArtist(userId: String, name: String) {
        _artistProfileState.update { it.copy(showSocialSheet = false) }
        navigate(Route.ArtistProfile(userId, name))
    }

    fun onSocialFollowPressed(userId: String) {
        val session = authRepository.sessionState.value
        if (session !is SessionState.Authenticated) {
            _artistProfileState.update { it.copy(showConnectPrompt = true) }
            return
        }

        val list = _artistProfileState.value.socialUsersList
        val targetUserIndex = list.indexOfFirst { it.id == userId }
        if (targetUserIndex == -1) return
        val targetUser = list[targetUserIndex]
        val currentlyFollowing = targetUser.doesCurrentUserFollow

        viewModelScope.launch {
            val response = if (currentlyFollowing) {
                apiClient.unfollowUser(userId)
            } else {
                apiClient.followUser(userId)
            }

            if (response?.error == null) {
                val updatedList = list.map { user ->
                    if (user.id == userId) {
                        user.copy(
                            doesCurrentUserFollow = !currentlyFollowing,
                            followerCount = if (currentlyFollowing) user.followerCount - 1 else user.followerCount + 1
                        )
                    } else {
                        user
                    }
                }
                _artistProfileState.update {
                    it.copy(socialUsersList = updatedList)
                }
            }
        }
    }

    fun popBack() {
        popBackStack()
    }
}
