package com.rld.justlisten.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.WorkManager
import com.rld.justlisten.di.appModule
import com.rld.justlisten.di.androidModule
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.JustListenAppPlatform
import com.rld.justlisten.ui.LocalMusicPlayer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinContext
import org.koin.core.context.GlobalContext.startKoin

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var musicPlayer: MusicPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            startKoin {
                androidContext(this@MainActivity)
                modules(appModule(), androidModule())
            }
        } catch (_: Exception) {
            // Koin already initialized
        }

        installSplashScreen()

        WorkManager.getInstance(applicationContext).cancelUniqueWork("SleepWorker")

        setContent {
            JustListenAppContent(musicPlayer = musicPlayer)
        }
    }
}

@Composable
fun JustListenAppContent(musicPlayer: MusicPlayer) {
    KoinContext {
        CompositionLocalProvider(LocalMusicPlayer provides musicPlayer) {
            JustListenAppPlatform()
        }
    }
}
