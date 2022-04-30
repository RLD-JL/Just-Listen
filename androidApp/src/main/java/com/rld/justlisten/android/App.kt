package com.rld.justlisten.android

import android.app.Application
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.*
import com.rld.justlisten.shared.viewmodel.getAndroidInstance
import com.rld.justlisten.android.exoplayer.MusicServiceConnection
import com.rld.justlisten.android.exoplayer.utils.Constants.CLICKED_PLAYLIST
import com.rld.justlisten.viewmodel.JustListenViewModel
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class JustListenApp : Application() {

    lateinit var model: JustListenViewModel

    @Inject
    lateinit var musicServiceConnection: MusicServiceConnection

    override fun onCreate() {
        super.onCreate()
        model = JustListenViewModel.Factory.getAndroidInstance(context = this)

        val appLifecycleObserver =
            AppLifecycleObserver(model = model, musicServiceConnection = musicServiceConnection)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }
}

class AppLifecycleObserver(
    private val model: JustListenViewModel,
    private val musicServiceConnection: MusicServiceConnection
) : LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                if (model.stateFlow.value.recompositionIndex > 0) { // not calling at app startup
                    musicServiceConnection.subscribe(
                        CLICKED_PLAYLIST,
                        object : MediaBrowserCompat.SubscriptionCallback() {
                            override fun onChildrenLoaded(
                                parentId: String,
                                children: List<MediaBrowserCompat.MediaItem>
                            ) {
                            }
                        }
                    )
                    model.navigation.onReEnterForeground()
                }
            }
            Lifecycle.Event.ON_STOP -> model.navigation.onEnterBackground()
            Lifecycle.Event.ON_DESTROY -> musicServiceConnection.unsubscribe(
                CLICKED_PLAYLIST,
                object : MediaBrowserCompat.SubscriptionCallback() {})
            else -> {}
        }
    }

}