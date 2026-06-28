package com.rld.justlisten.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.rld.justlisten.BuildConfig
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.utils.isIos
import org.koin.compose.koinInject

/**
 * Main app composable that can be used across all platforms
 */
@Composable
fun JustListenApp(
    modifier: Modifier = Modifier,
    musicPlayer: MusicPlayer = koinInject(),
) {
    val navController = rememberNavController()
    val settingsState by koinInject<com.rld.justlisten.viewmodel.settings.SettingsViewModel>().settingsState.collectAsState()

    CompositionLocalProvider(
        LocalMusicPlayer provides musicPlayer
    ) {
        val customColors = com.rld.justlisten.ui.theme.CustomThemeColors(
            primary = settingsState.customPrimary,
            secondary = settingsState.customSecondary,
            background = settingsState.customBackground,
            surface = settingsState.customSurface,
        )
        JustListenTheme(
            darkTheme = settingsState.isDarkThemeOn,
            palletColor = settingsState.palletColor,
            customColors = customColors,
        ) {
            Surface(
                modifier = modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                if (!settingsState.isSettingsLoaded) {
                    Box(modifier = Modifier.fillMaxSize())
                } else {
                    val startDestination = if (settingsState.isFirstLaunch) {
                        com.rld.justlisten.navigation.Route.Onboarding
                    } else {
                        com.rld.justlisten.navigation.Route.Playlist
                    }
                    JustListenScaffold(
                        navController = navController,
                        showSupportTab = settingsState.hasSupportNavigationOn && !BuildConfig.IS_PLAYSTORE_BUILD && !isIos,
                        startDestination = startDestination,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
