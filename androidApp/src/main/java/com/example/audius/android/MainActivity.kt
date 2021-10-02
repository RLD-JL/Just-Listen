package com.example.audius.android

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.primarySurface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import com.example.audius.android.ui.MainComposable
import com.example.audius.android.ui.theme.utils.AppThemeState
import com.example.audius.android.ui.theme.AudiusTheme
import com.example.audius.android.ui.theme.ColorPallet


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model = (application as AudiusApp).model
        val musicServiceConnection = (application as AudiusApp).musicServiceConnection

        setContent {
            AudiusTheme(darkTheme = true, colorPallet = ColorPallet.DARK ) {
                window.statusBarColor = MaterialTheme.colors.background.toArgb()
                window.navigationBarColor = MaterialTheme.colors.primaryVariant.toArgb()
                MainComposable(model, musicServiceConnection)
            }
        }
    }
}




