package com.example.justlisten.android.ui.loadingscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LoadingScreen(padding: Dp = 0.dp) {
   Box(modifier = Modifier.fillMaxSize().padding(top = padding)) {
       CircularProgressIndicator(modifier = Modifier.align(alignment = Alignment.Center))
   }
}