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
    val musicSource: MusicSource,
    private val musicPreloader: MusicPreloader,
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

    private val _playWhenReady: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val playWhenReady = _playWhenReady.asStateFlow()

    private val _shuffleModeEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val shuffleModeEnabled = _shuffleModeEnabled.asStateFlow()

    private val _repeatMode: MutableStateFlow<Int> = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode = _repeatMode.asStateFlow()

    val sliderClicked: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val _currentPlayingSong: MutableStateFlow<MediaItem?> = MutableStateFlow(null)
    val currentPlayingSong = _currentPlayingSong.asStateFlow()

    private val _currentPlaylist: MutableStateFlow<List<MediaItem>> = MutableStateFlow(emptyList())
    val currentPlaylist = _currentPlaylist.asStateFlow()

    var mediaController: MediaController? = null
    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
    private val playerListener = PlayerListener()

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var updateJob: Job? = null

    init {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future
        future.addListener({
            try {
                mediaController = future.get()
                mediaController?.addListener(playerListener)
                _isConnected.value = true
                updateSong()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    fun updatePlaylist(list: List<Item>, startIndex: Int = 0) {
        musicSource.playlist = list
        musicSource.fetchMediaData()
        mediaController?.setMediaItems(musicSource.songs, startIndex, 0L)
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

        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
            val list = mutableListOf<MediaItem>()
            mediaController?.let { controller ->
                for (i in 0 until controller.mediaItemCount) {
                    list.add(controller.getMediaItemAt(i))
                }
            }
            _currentPlaylist.value = list
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            _playWhenReady.value = playWhenReady
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _shuffleModeEnabled.value = shuffleModeEnabled
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = repeatMode
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _currentPlayingSong.value = mediaItem
            mediaItem?.let {
                preloadNextSong()
            }
        }
        
        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            _networkError.value = true
        }
    }

    private fun preloadNextSong() {
        val controller = mediaController ?: return
        val nextIndex = controller.nextMediaItemIndex
        if (nextIndex != androidx.media3.common.C.INDEX_UNSET && nextIndex < controller.mediaItemCount) {
            val nextSong = controller.getMediaItemAt(nextIndex)
            musicPreloader.preloadSong(nextSong.mediaId)
        }
    }

    fun release() {
        serviceJob.cancel()
        updateJob?.cancel()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        mediaController = null
        _isConnected.value = false
    }
}
