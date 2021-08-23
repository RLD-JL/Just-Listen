package com.example.audius.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.audius.android.ui.MainComposable


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model = (application as AudiusApp).model

        setContent {
            MainComposable(model)
        }
    }
}




