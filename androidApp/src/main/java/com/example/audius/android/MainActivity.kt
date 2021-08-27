package com.example.audius.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.MainComposable
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject



@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var musicServiceConnection: MusicServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model = (application as AudiusApp).model

        setContent {
            MainComposable(model, musicServiceConnection)
        }
    }
}




