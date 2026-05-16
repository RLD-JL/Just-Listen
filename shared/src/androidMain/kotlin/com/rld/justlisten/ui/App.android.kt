package com.rld.justlisten.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.rld.justlisten.datalayer.Repository
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import com.rld.justlisten.viewmodel.settings.SettingsViewModel

@Composable
actual fun JustListenAppPlatform(modifier: Modifier) {
    val musicPlayer = LocalMusicPlayer.current
    val repository: Repository = koinInject()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settingsState by settingsViewModel.settingsState.collectAsState()

    JustListenApp(
        musicPlayer = musicPlayer,
        showDonationTab = settingsState.hasDonationNavigationOn,
        darkTheme = settingsState.isDarkThemeOn,
        repository = repository,
        modifier = modifier,
    )
}
