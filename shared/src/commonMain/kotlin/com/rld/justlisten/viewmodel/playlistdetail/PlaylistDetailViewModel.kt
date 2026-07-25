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
    private val repostOperations = mutableMapOf<String, RepostOperation>()

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
                        item.copy(
                            isReposted = repostOperations[item.id]?.desiredState
                                ?: repostedIds.contains(item.id)
                        )
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
        // The database and Audius sync are background work; reflect the tap immediately.
        _playlistDetailState.update { state ->
            state.copy(
                songPlaylist = state.songPlaylist.map { item ->
                    if (item.id == id) item.copy(isFavorite = isFavorite) else item
                }
            )
        }
        viewModelScope.launch {
            favoritesRepository.saveSongToFavorites(id, title, user, songIconList, "Favorite", isFavorite)
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
                    playlistId = args.playlistId,
                    playlistEnum = args.playlistEnum
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

    fun removeSongFromPlaylist(playlistName: String, songId: String) {
        viewModelScope.launch {
            val playlist = libraryRepository.getAddPlaylist().firstOrNull { it.playlistName == playlistName }
            if (playlist != null) {
                val updatedSongs = (playlist.songsList ?: emptyList()).filter { it != songId }
                libraryRepository.updatePlaylistSongs(
                    playlistName = playlistName,
                    playlistDescription = playlist.playlistDescription,
                    songList = updatedSongs,
                    isRemote = playlist.isRemote,
                    isPrivate = playlist.isPrivate,
                    playlistId = playlist.playlistId
                )
                _playlistDetailState.update { state ->
                    val updatedList = state.songPlaylist.filter { it.id != songId }
                    state.copy(songPlaylist = updatedList)
                }
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

        val currentItem = _playlistDetailState.value.songPlaylist.firstOrNull { it.id == id }
            ?: return
        val existingOperation = repostOperations[id]
        val operation = existingOperation ?: RepostOperation(
            desiredState = isRepost,
            confirmedState = currentItem.isReposted,
            confirmedCount = currentItem.repostCount,
        ).also { repostOperations[id] = it }
        operation.desiredState = isRepost
        updateVisibleRepost(id, isRepost)

        // Coalesce rapid taps into one serial worker so slow responses cannot overwrite
        // the user's latest choice.
        if (existingOperation != null) return
        viewModelScope.launch {
            try {
                while (true) {
                    val activeOperation = repostOperations[id] ?: break
                    val requestedState = activeOperation.desiredState
                    if (requestedState != activeOperation.confirmedState) {
                        val success = if (requestedState) {
                            playlistRepository.repostTrack(id)
                        } else {
                            playlistRepository.unrepostTrack(id)
                        }
                        if (success) {
                            activeOperation.confirmedCount = (
                                activeOperation.confirmedCount + if (requestedState) 1 else -1
                            ).coerceAtLeast(0)
                            activeOperation.confirmedState = requestedState
                        } else if (activeOperation.desiredState == requestedState) {
                            setVisibleRepost(
                                id = id,
                                isReposted = activeOperation.confirmedState,
                                repostCount = activeOperation.confirmedCount,
                            )
                            break
                        }
                    }

                    if (activeOperation.desiredState == activeOperation.confirmedState) {
                        setVisibleRepost(
                            id = id,
                            isReposted = activeOperation.confirmedState,
                            repostCount = activeOperation.confirmedCount,
                        )
                        break
                    }
                }
            } finally {
                repostOperations.remove(id)
            }
        }
    }

    private fun updateVisibleRepost(id: String, isReposted: Boolean) {
        _playlistDetailState.update { state ->
            state.copy(
                songPlaylist = state.songPlaylist.map { item ->
                    if (item.id != id || item.isReposted == isReposted) {
                        item
                    } else {
                        val newCount = (
                            item.repostCount + if (isReposted) 1 else -1
                        ).coerceAtLeast(0)
                        item.copy(
                            _data = item._data.copy(repostCount = newCount),
                            isReposted = isReposted,
                        )
                    }
                }
            )
        }
    }

    private fun setVisibleRepost(id: String, isReposted: Boolean, repostCount: Int) {
        _playlistDetailState.update { state ->
            state.copy(
                songPlaylist = state.songPlaylist.map { item ->
                    if (item.id == id) {
                        item.copy(
                            _data = item._data.copy(repostCount = repostCount),
                            isReposted = isReposted,
                        )
                    } else {
                        item
                    }
                }
            )
        }
    }

    fun dismissConnectPrompt() {
        _playlistDetailState.update { it.copy(showConnectPrompt = false) }
    }
}

private data class RepostOperation(
    var desiredState: Boolean,
    var confirmedState: Boolean,
    var confirmedCount: Int,
)
