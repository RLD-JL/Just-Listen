package com.rld.justlisten.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.WorkManager
import com.rld.justlisten.ui.JustListenApp
import org.koin.compose.KoinContext
import org.koin.mp.KoinPlatform.stopKoin

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        installSplashScreen()
        
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.cancelUniqueWork("SleepWorker")

        setContent {
            JustListenAppContent()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up Koin to prevent memory leaks
        // Only stop if this is not a configuration change
        if (isFinishing) {
            stopKoin()
        }
    }
}

@Composable
fun JustListenAppContent() {
    KoinContext {
        JustListenApp()
    }
}




