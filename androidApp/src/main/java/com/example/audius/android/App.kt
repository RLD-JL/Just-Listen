package com.example.audius.android

import android.app.Application
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import coil.ImageLoader
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.exoplayer.utils.Constants.MEDIA_ROOT_ID
import com.example.audius.shared.viewmodel.getAndroidInstance
import com.example.audius.viewmodel.AudiusViewModel
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AudiusApp : Application() {

    lateinit var model: AudiusViewModel

    @Inject
    lateinit var musicServiceConnection: MusicServiceConnection

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()
        model = AudiusViewModel.Factory.getAndroidInstance(context = this)

        val appLifecycleObserver =
            AppLifecycleObserver(model = model, musicServiceConnection = musicServiceConnection)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }
}

class AppLifecycleObserver(
    private val model: AudiusViewModel,
    private val musicServiceConnection: MusicServiceConnection
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        if (model.stateFlow.value.recompositionIndex > 0) { // not calling at app startup
            model.navigation.onReEnterForeground()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        model.navigation.onEnterBackground()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroyed() {
        musicServiceConnection.unsubscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }

}