package com.rld.justlisten.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.rld.justlisten.di.appModule
import com.rld.justlisten.di.androidModule

class JustListenApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@JustListenApp)
            modules(androidModule(BuildConfig.AUDIUS_API_KEY), appModule())
        }
    }
}
