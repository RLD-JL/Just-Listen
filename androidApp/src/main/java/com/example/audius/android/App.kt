package com.example.audius.android

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.audius.shared.viewmodel.getAndroidInstance
import com.example.audius.viewmodel.AudiusViewModel
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AudiusApp : Application() {

    lateinit var model: AudiusViewModel

    override fun onCreate() {
        super.onCreate()
        model = AudiusViewModel.Factory.getAndroidInstance(context = this)

        val appLifecycleObserver = AppLifecycleObserver(model = model)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }
}

class AppLifecycleObserver (private val model: AudiusViewModel) : LifecycleObserver {

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

}