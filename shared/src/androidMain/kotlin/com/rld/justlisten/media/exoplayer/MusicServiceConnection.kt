package com.rld.justlisten.media.exoplayer

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.rld.justlisten.media.exoplayer.utils.Constants.NETWORK_ERROR
import com.rld.justlisten.viewmodel.interfaces.Item
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicServiceConnection(
    private val musicSource: MusicSource,
    context: Context
) {

    private val _isConnected: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    val isFavorite: MutableMap<String, Boolean> = mutableMapOf()

    private val _networkError: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val networkError = _networkError.asStateFlow()

    private val _playbackPosition: MutableStateFlow<Long> = MutableStateFlow(0)
    val playbackPosition = _playbackPosition.asStateFlow()

    private val _playbackState: MutableStateFlow<Int> = MutableStateFlow(Player.STATE_IDLE)
    val playbackState = _playbackState.asStateFlow()

    val sliderClicked: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val _currentPlayingSong: MutableStateFlow<MediaItem?> = MutableStateFlow(null)
    val currentPlayingSong = _currentPlayingSong.asStateFlow()

    var mediaController: MediaController? = null

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var updateJob: Job? = null

    init {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            mediaController?.addListener(PlayerListener())
            _isConnected.value = true
            updateSong()
        }, MoreExecutors.directExecutor())
    }

    fun updatePlaylist(list: List<Item>) {
        musicSource.playlist = list
        musicSource.fetchMediaData()
        mediaController?.setMediaItems(musicSource.songs)
        mediaController?.prepare()
    }

    fun updateSong() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                if (!sliderClicked.value) {
                    val pos = mediaController?.currentPosition ?: 0L
                    if (_playbackPosition.value != pos) {
                        _playbackPosition.value = pos
                    }
                }
                delay(1000L)
            }
        }
    }

    private inner class PlayerListener : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            _playbackState.value = state
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _currentPlayingSong.value = mediaItem
        }
        
        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            _networkError.value = true
        }
    }
}
