package com.rld.justlisten.android.exoplayer.callbacks

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.rld.justlisten.android.exoplayer.MusicService
import com.rld.justlisten.android.exoplayer.utils.Constants.NOTIFICATION_ID
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MusicPlayerNotificationListener(
    private val musicService: MusicService
) : PlayerNotificationManager.NotificationListener {
    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)
        musicService.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                musicService.stopForeground(Service.STOP_FOREGROUND_DETACH)
            } else {
                musicService.stopForeground(true)
            }
            isForegroundService = false
            stopSelf()
        }
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)
        musicService.apply {
            if(ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    this,
                    Intent(applicationContext, this::class.java)
                )
                startForeground(NOTIFICATION_ID, notification)
                isForegroundService = true
            }
        }
    }
}