package com.rld.justlisten.android

import android.app.Application
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.rld.justlisten.media.exoplayer.MusicServiceConnection
import com.rld.justlisten.media.exoplayer.utils.Constants.CLICKED_PLAYLIST
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class JustListenApp : Application() {

    @Inject
    lateinit var musicServiceConnection: MusicServiceConnection

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            AppLifecycleObserver(musicServiceConnection),
        )
    }
}

class AppLifecycleObserver(
    private val musicServiceConnection: MusicServiceConnection,
) : LifecycleEventObserver {

    private var hasEnteredForegroundBefore = false

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                if (hasEnteredForegroundBefore) {
                    musicServiceConnection.subscribe(
                        CLICKED_PLAYLIST,
                        object : MediaBrowserCompat.SubscriptionCallback() {
                            override fun onChildrenLoaded(
                                parentId: String,
                                children: List<MediaBrowserCompat.MediaItem>,
                            ) {
                            }
                        },
                    )
                }
                hasEnteredForegroundBefore = true
            }
            Lifecycle.Event.ON_DESTROY -> {
                musicServiceConnection.unsubscribe(
                    CLICKED_PLAYLIST,
                    object : MediaBrowserCompat.SubscriptionCallback() {},
                )
            }
            else -> Unit
        }
    }
}
