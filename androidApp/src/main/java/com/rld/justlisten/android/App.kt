package com.rld.justlisten.android

import android.app.Application
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.rld.justlisten.media.exoplayer.MusicServiceConnection
import com.rld.justlisten.media.exoplayer.utils.Constants.CLICKED_PLAYLIST
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.rld.justlisten.di.appModule
import com.rld.justlisten.di.androidModule

class JustListenApp : Application() {

    val musicServiceConnection: MusicServiceConnection by inject()

    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@JustListenApp)
            modules(appModule(), androidModule())
        }
        
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
