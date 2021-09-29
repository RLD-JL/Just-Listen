package com.example.audius.android.ui.loadingscreen

import android.widget.ProgressBar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LoadingScreen() {
   Box(modifier = Modifier.fillMaxSize()) {
       CircularProgressIndicator(modifier = Modifier.align(alignment = Alignment.Center))
   }
}