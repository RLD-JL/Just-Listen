package com.rld.justlisten.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rld.justlisten.android.ui.MainComposable
import com.rld.justlisten.android.ui.theme.JustListenTheme
import com.rld.justlisten.android.ui.theme.ColorPallet


class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model = (application as JustListenApp).model
        val musicServiceConnection = (application as JustListenApp).musicServiceConnection
        installSplashScreen().apply {
            
        }
        setContent {
            JustListenTheme(darkTheme = true, colorPallet = ColorPallet.DARK ) {
                window.statusBarColor = MaterialTheme.colors.background.toArgb()
                window.navigationBarColor = MaterialTheme.colors.primaryVariant.toArgb()
                MainComposable(model, musicServiceConnection)
            }
        }
    }
}




