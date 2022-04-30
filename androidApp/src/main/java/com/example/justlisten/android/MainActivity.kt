package com.example.justlisten.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import com.example.justlisten.android.ui.MainComposable
import com.example.justlisten.android.ui.theme.JustListenTheme
import com.example.justlisten.android.ui.theme.ColorPallet


class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model = (application as JustListenApp).model
        val musicServiceConnection = (application as JustListenApp).musicServiceConnection

        setContent {
            JustListenTheme(darkTheme = true, colorPallet = ColorPallet.DARK ) {
                window.statusBarColor = MaterialTheme.colors.background.toArgb()
                window.navigationBarColor = MaterialTheme.colors.primaryVariant.toArgb()
                MainComposable(model, musicServiceConnection)
            }
        }
    }
}




