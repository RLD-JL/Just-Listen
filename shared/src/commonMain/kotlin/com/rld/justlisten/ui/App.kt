package com.rld.justlisten.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rld.justlisten.media.MusicPlayer

@Composable
expect fun JustListenAppPlatform(modifier: Modifier = Modifier)

@Composable
fun JustListenApp(
    musicPlayer: MusicPlayer,
    showDonationTab: Boolean,
    darkTheme: Boolean,
    repository: com.rld.justlisten.datalayer.Repository,
    modifier: Modifier = Modifier,
) {
    JustListenTheme(darkTheme = darkTheme) {
        JustListenAppContent(
            musicPlayer = musicPlayer,
            showDonationTab = showDonationTab,
            repository = repository,
            modifier = modifier,
        )
    }
}
