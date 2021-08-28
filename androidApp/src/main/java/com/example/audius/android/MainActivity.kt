package com.example.audius.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.MainComposable
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model = (application as AudiusApp).model
        val musicServiceConnection = (application as AudiusApp).musicServiceConnection

        setContent {
            MainComposable(model, musicServiceConnection)
        }
    }
}




