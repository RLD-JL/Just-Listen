package com.example.audius.android.exoplayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.compose.ui.res.stringResource
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.audius.android.R
import com.example.audius.android.exoplayer.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.example.audius.android.exoplayer.utils.Constants.NOTIFICATION_ID
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MusicNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener,
    private val newSongCallback: () -> Unit
) {

    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)

        notificationManager = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID,
        ).setNotificationListener(notificationListener)
            .setSmallIconResourceId(R.drawable.exo_notification_small_icon)
            .setMediaDescriptionAdapter(DescriptionAdapter(mediaController)
        ).build()
    }

    private inner class DescriptionAdapter(
        private val mediaController: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
           return mediaController.metadata.description.subtitle.toString()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            var bitmap: Bitmap
            ImageRequest.Builder(context)
                .data(mediaController.metadata.description.mediaUri) // demo link
                .target { result ->
                     bitmap = (result as BitmapDrawable).bitmap
                    callback.onBitmap(bitmap)
                }
                .build()
        return null
        }
    }


}
