package com.example.audius.android.exoplayer.callbacks

import android.widget.Toast
import com.example.audius.android.exoplayer.MusicNotificationManager
import com.example.audius.android.exoplayer.MusicService
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player

class MusicPlayerEventListener(
    private val musicService: MusicService,
    private val notificationManager: MusicNotificationManager,
    private val exoPlayer: ExoPlayer
) : Player.Listener {
    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        if (reason == Player.STATE_READY && !playWhenReady) {
            musicService.stopForeground(false)
            musicService.isForegroundService = false
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService, "Error", Toast.LENGTH_SHORT).show()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING,
            Player.STATE_READY -> {
                notificationManager.showNotification(exoPlayer)
            }
            else -> {
                notificationManager.hideNotification()
            }
        }
    }
}