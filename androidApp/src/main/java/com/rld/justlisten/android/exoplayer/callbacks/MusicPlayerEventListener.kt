package com.rld.justlisten.android.exoplayer.callbacks

import android.app.Service.STOP_FOREGROUND_DETACH
import android.os.Build
import android.widget.Toast
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.DISCONTINUITY_REASON_AUTO_TRANSITION
import com.rld.justlisten.android.exoplayer.MusicNotificationManager
import com.rld.justlisten.android.exoplayer.MusicService

class MusicPlayerEventListener(
    private val musicService: MusicService,
    private val notificationManager: MusicNotificationManager,
    private val exoPlayer: ExoPlayer,
    val songHasRepeated: () -> Unit
) : Player.Listener {
    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        if (reason == Player.STATE_READY && !playWhenReady) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                musicService.stopForeground(STOP_FOREGROUND_DETACH)
            } else {
                musicService.stopForeground(true)
            }
            musicService.isForegroundService = false
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(
            musicService,
            "Unfortunately this song has issues, try another one",
            Toast.LENGTH_SHORT
        ).show()
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

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {

        if (exoPlayer.repeatMode == Player.REPEAT_MODE_ONE) {
            if (reason == DISCONTINUITY_REASON_AUTO_TRANSITION) {
                songHasRepeated()
            }
        }
    }
}