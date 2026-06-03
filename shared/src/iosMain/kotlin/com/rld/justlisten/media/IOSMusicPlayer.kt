@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package com.rld.justlisten.media

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.rld.justlisten.viewmodel.interfaces.Item
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import platform.AVFoundation.*
import platform.AVFAudio.*
import platform.Foundation.NSURL
import platform.Foundation.NSNotificationCenter
import platform.darwin.NSObject
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.CMTimeGetSeconds

class IOSMusicPlayer(
    private val favoritesRepository: FavoritesRepository
) : MusicPlayer {
    private var avPlayer: AVPlayer? = null
    private var playlistItems = mutableListOf<MediaMetadata>()
    private var currentIndex = -1
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        try {
            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryPlayback, error = null)
            audioSession.setActive(true, error = null)
        } catch (e: Exception) {
            // Ignore session activation errors
        }

        scope.launch {
            while (isActive) {
                updateProgress()
                delay(250L)
            }
        }

        scope.launch {
            favoritesRepository.getFavoritePlaylistFlow().collect { favoriteList ->
                val favoriteIds = favoriteList.map { it.id }.toSet()
                
                // Update current media favorite status
                val currentMedia = _playbackState.value.currentMedia
                if (currentMedia != null) {
                    val isFav = favoriteIds.contains(currentMedia.id)
                    if (isFav != currentMedia.isFavorite) {
                        _playbackState.update { state ->
                            state.copy(
                                currentMedia = state.currentMedia?.copy(isFavorite = isFav)
                            )
                        }
                    }
                }

                // Update current playlist items favorite status
                playlistItems = playlistItems.map { item ->
                    item.copy(isFavorite = favoriteIds.contains(item.id))
                }.toMutableList()
                _currentPlaylist.value = playlistItems
            }
        }
    }

    override var currentlyPlayingPlaylistId: String? = null

    private val _playbackState = MutableStateFlow(
        PlaybackState(PlaybackStatus.IDLE, 0L, null, false, RepeatMode.NONE)
    )
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<MediaMetadata>>(emptyList())
    override val currentPlaylist: StateFlow<List<MediaMetadata>> = _currentPlaylist.asStateFlow()

    private val _isConnected = MutableStateFlow(true)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _networkError = MutableStateFlow(false)
    override val networkError: StateFlow<Boolean> = _networkError.asStateFlow()

    override fun play() {
        avPlayer?.play()
        updateState(PlaybackStatus.PLAYING)
    }

    override fun pause() {
        avPlayer?.pause()
        updateState(PlaybackStatus.PAUSED)
    }

    override fun stop() {
        avPlayer?.pause()
        avPlayer = null
        updateState(PlaybackStatus.STOPPED)
    }

    override fun playMedia(mediaId: String) {
        val index = playlistItems.indexOfFirst { it.id == mediaId }
        if (index != -1) {
            currentIndex = index
            playTrack(playlistItems[index])
        }
    }

    private fun playTrack(metadata: MediaMetadata) {
        // Audius streaming URL format (using Audius API)
        val streamUrl = "https://api.audius.co/v1/tracks/${metadata.id}/stream?app_name=JustListen"
        val nsUrl = NSURL.URLWithString(streamUrl)
        if (nsUrl != null) {
            val playerItem = AVPlayerItem.playerItemWithURL(nsUrl)
            avPlayer = AVPlayer.playerWithPlayerItem(playerItem)
            avPlayer?.play()
            updateState(PlaybackStatus.PLAYING, metadata)
        } else {
            updateState(PlaybackStatus.ERROR)
        }
    }

    private fun updateState(status: PlaybackStatus, currentMedia: MediaMetadata? = _playbackState.value.currentMedia) {
        _playbackState.update { state ->
            state.copy(
                status = status,
                currentMedia = currentMedia ?: state.currentMedia
            )
        }
    }

    override fun skipToNext() {
        if (currentIndex < playlistItems.size - 1) {
            currentIndex++
            playTrack(playlistItems[currentIndex])
        }
    }

    override fun skipToPrevious() {
        if (currentIndex > 0) {
            currentIndex--
            playTrack(playlistItems[currentIndex])
        }
    }

    override fun seekTo(position: Long) {
        val time = CMTimeMake(position, 1000)
        avPlayer?.seekToTime(time)
    }

    override fun updatePlaylist(list: List<Item>) {
        playlistItems = list.map {
            MediaMetadata(
                id = it.id,
                title = it.title,
                artist = it.user,
                duration = 0L,
                artworkUrl = it.songIconList.songImageURL480px,
                lowResArtworkUrl = it.songIconList.songImageURL150px,
                isFavorite = favoritesRepository.getFavoritePlaylistWithId(it.id) != null
            )
        }.toMutableList()
        _currentPlaylist.value = playlistItems
    }

    override fun setShuffleModeEnabled(enabled: Boolean) {}
    override fun setRepeatMode(repeatMode: RepeatMode) {}
    override fun refreshMetadata() {
        _playbackState.update { state ->
            val currentMedia = state.currentMedia
            if (currentMedia != null) {
                val isFav = favoritesRepository.getFavoritePlaylistWithId(currentMedia.id) != null
                state.copy(
                    currentMedia = currentMedia.copy(isFavorite = isFav)
                )
            } else {
                state
            }
        }
    }
    override fun removeTrack(index: Int) {
        if (index in playlistItems.indices) {
            playlistItems.removeAt(index)
            _currentPlaylist.value = playlistItems
        }
    }
    override fun moveTrack(fromIndex: Int, toIndex: Int) {}

    private fun updateProgress() {
        val player = avPlayer ?: return
        val currentItem = player.currentItem ?: return

        val durationTime = currentItem.duration
        val durationSec = CMTimeGetSeconds(durationTime)
        val durationMs = if (!durationSec.isNaN() && durationSec > 0.0) {
            (durationSec * 1000).toLong()
        } else {
            0L
        }

        val currentTime = player.currentTime()
        val currentSec = CMTimeGetSeconds(currentTime)
        val currentMs = if (!currentSec.isNaN() && currentSec > 0.0) {
            (currentSec * 1000).toLong()
        } else {
            0L
        }

        _playbackState.update { state ->
            val updatedMedia = state.currentMedia?.copy(duration = durationMs)
            state.copy(
                currentPosition = currentMs,
                currentMedia = updatedMedia
            )
        }
    }
}
