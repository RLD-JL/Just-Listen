package com.rld.justlisten.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.WorkManager
import com.rld.justlisten.ui.JustListenApp
import com.rld.justlisten.ui.JustListenApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        installSplashScreen()
        
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.cancelUniqueWork("SleepWorker")

        setContent {
            JustListenAppContent()
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        val data: android.net.Uri? = intent?.data
        if (data != null && data.scheme == "justlisten" && data.host == "oauth") {
            val code = data.getQueryParameter("code")
            if (code != null) {
                val redirectUri = "justlisten://oauth/callback"
                try {
                    val settingsViewModel = org.koin.mp.KoinPlatform.getKoin().get<com.rld.justlisten.viewmodel.settings.SettingsViewModel>()
                    settingsViewModel.loginWithCode(code, redirectUri)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

@Composable
fun JustListenAppContent() {
    JustListenApp()
}




