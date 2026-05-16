package com.rld.justlisten.media.exoplayer

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.rld.justlisten.media.exoplayer.library.extension.isFavorite
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

    private val _playbackState: MutableStateFlow<PlaybackStateCompat?> =
        MutableStateFlow(PlaybackStateCompat.fromPlaybackState(null))
    val playbackState = _playbackState.asStateFlow()

    val sliderClicked: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val _currentPlayingSong: MutableStateFlow<MediaMetadataCompat?> =
        MutableStateFlow(MediaMetadataCompat.fromMediaMetadata(null))
    val currentPlayingSong = _currentPlayingSong.asStateFlow()

    lateinit var mediaController: MediaControllerCompat

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var updateJob: Job? = null

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context = context)

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            MusicService::class.java
        ),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        connect()
        updateSong()
    }

    val transportControls: MediaControllerCompat.TransportControls?
        get() = if (isConnected.value) mediaController.transportControls else null

    val shuffleMode: Int
        get() = if (isConnected.value) mediaController.shuffleMode else PlaybackStateCompat.SHUFFLE_MODE_NONE

    val repeatMode: Int
        get() = if (isConnected.value) mediaController.repeatMode else PlaybackStateCompat.REPEAT_MODE_NONE

    fun updatePlaylist(list: List<Item>) {
        musicSource.playlist = list
        musicSource.fetchMediaData()
    }


    fun updateSong() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                if (!sliderClicked.value) {
                    val pos = playbackState.value?.currentPlaybackPosition ?: 0L
                    if (_playbackPosition.value != pos) {
                        _playbackPosition.value = pos
                    }
                }
                delay(1000L)
            }
        }
    }

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            _isConnected.value = true
        }

        override fun onConnectionSuspended() {
            _isConnected.value = false
        }

        override fun onConnectionFailed() {
            _isConnected.value = false
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.value = state
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _currentPlayingSong.value = metadata

            _currentPlayingSong.value?.description?.mediaId?.let {
                isFavorite.put(
                    it,
                    isFavorite[it] ?: currentPlayingSong.value?.isFavorite.toBoolean()
                )
            }
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            when (event) {
                NETWORK_ERROR -> _networkError.value = true
            }
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}
