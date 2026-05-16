package com.rld.justlisten.media.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.compose.runtime.mutableStateOf
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.rld.justlisten.media.exoplayer.callbacks.MusicPlaybackPreparer
import com.rld.justlisten.media.exoplayer.callbacks.MusicPlayerEventListener
import com.rld.justlisten.media.exoplayer.callbacks.MusicPlayerNotificationListener
import com.rld.justlisten.media.exoplayer.library.extension.toMediaItem
import com.rld.justlisten.media.exoplayer.utils.Constants.MEDIA_ROOT_ID
import com.rld.justlisten.media.exoplayer.utils.Constants.NETWORK_ERROR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.inject

private const val SERVICE_TAG = "MusicService"


class MusicService : MediaBrowserServiceCompat() {

    val exoPlayer: ExoPlayer by inject()

    private lateinit var musicNotificationManager: MusicNotificationManager

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private var currentPlaylistItems: List<MediaMetadataCompat> = emptyList()

    var isForegroundService = false

    val musicSource: MusicSource by inject()

    private var curPlayingSong: MediaMetadataCompat? = null

    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    companion object {
        var curSongDuration = 0L
            private set
        val songHasRepeated = mutableStateOf(false)
    }

    override fun onCreate() {
        super.onCreate()



        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ) {
            curSongDuration = exoPlayer.duration
        }

        val musicPlaybackPreparer = MusicPlaybackPreparer(musicSource) {
            curPlayingSong = it
            preparePlayer(musicSource.songs, it, true)
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator(mediaSession))
        mediaSessionConnector.setPlayer(exoPlayer)

        musicNotificationManager.showNotification(exoPlayer)

        musicPlayerEventListener = MusicPlayerEventListener(this, musicNotificationManager, exoPlayer) {
            songHasRepeated.value = true
        }
        exoPlayer.addListener(musicPlayerEventListener)
    }

    private inner class MusicQueueNavigator(mediaSessionCompat: MediaSessionCompat) : TimelineQueueNavigator(mediaSessionCompat) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            if (windowIndex < currentPlaylistItems.size) {
                return currentPlaylistItems[windowIndex].description
            }
            return MediaDescriptionCompat.Builder().build()
        }
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val currentSongIndex = if (curPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        currentPlaylistItems = songs

        exoPlayer.stop()
        exoPlayer.setMediaItems(songs.map {
            it.toMediaItem()}, currentSongIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }
        serviceScope.cancel()
        musicNotificationManager.release()
        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
        super.onDestroy()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            MEDIA_ROOT_ID -> {
            }
            else -> {
                val resultsSent = musicSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        val item = musicSource.songs.map { item ->
                            MediaBrowserCompat.MediaItem(item.description,
                                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                            )
                        }
                        result.sendResult(item)
                    } else {
                        mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                        result.sendResult(null)
                    }
                }
                if (!resultsSent) {
                    result.detach()
                }
            }
        }
    }
}
