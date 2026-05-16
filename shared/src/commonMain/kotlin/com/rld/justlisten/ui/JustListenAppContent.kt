package com.rld.justlisten.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.media.MusicPlayer

@Composable
fun JustListenAppContent(
    musicPlayer: MusicPlayer,
    showDonationTab: Boolean,
    repository: Repository,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colors.background,
    ) {
        JustListenScaffold(
            navController = navController,
            musicPlayer = musicPlayer,
            showDonationTab = showDonationTab,
            repository = repository,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
