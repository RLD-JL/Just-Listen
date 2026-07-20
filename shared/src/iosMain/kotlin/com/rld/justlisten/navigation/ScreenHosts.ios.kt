package com.rld.justlisten.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import androidx.navigation.compose.rememberNavController
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.JustListenApp
import com.rld.justlisten.ui.JustListenTheme
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.viewmodel.player.PlayerViewModel
import com.rld.justlisten.viewmodel.settings.SettingsViewModel
import org.koin.compose.koinInject
import platform.UIKit.UIViewController

// Existing pre-iOS 26 entry point.
@OptIn(ExperimentalComposeUiApi::class)
fun MainViewController(): UIViewController = ComposeUIViewController(
    configure = { parallelRendering = true }
) {
    JustListenApp()
}

// Single-screen entry point used by SwiftUI's native navigation containers.
@OptIn(ExperimentalComposeUiApi::class)
fun ScreenViewController(
    route: Route,
    bottomSafeArea: Double,
    onNavigate: (Route) -> Unit,
    onPopBackStack: () -> Unit
): UIViewController = ComposeUIViewController(
    configure = { parallelRendering = true }
) {
    val musicPlayer: MusicPlayer = koinInject()
    val settingsState by koinInject<SettingsViewModel>().settingsState.collectAsState()
    val playerViewModel = koinInject<PlayerViewModel>()
    val playerUiState by playerViewModel.playerUiState.collectAsState()
    val callbacks = IosNavigationCallbacks(onNavigate, onPopBackStack)
    val navController = rememberNavController()

    CompositionLocalProvider(
        LocalMusicPlayer provides musicPlayer,
        LocalIosNavigationCallbacks provides callbacks,
        LocalUseNativeNavigation provides true
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
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent
                ) { innerPadding ->
                    val playbackState = playerUiState.playbackState
                        ?: com.rld.justlisten.media.PlaybackState(
                            status = com.rld.justlisten.media.PlaybackStatus.IDLE,
                            currentPosition = 0
                        )
                    val shouldShowPlayBar =
                        playbackState.status == com.rld.justlisten.media.PlaybackStatus.PLAYING ||
                            playbackState.status == com.rld.justlisten.media.PlaybackStatus.PAUSED ||
                            playbackState.status == com.rld.justlisten.media.PlaybackStatus.BUFFERING ||
                            playbackState.currentMedia != null

                    val extraBottom = if (shouldShowPlayBar) {
                        IosPlayerLayoutMetrics.miniPlayerHeight
                    } else {
                        0.dp
                    }
                    val bottomPadding = bottomSafeArea.dp +
                        IosPlayerLayoutMetrics.tabBarContentHeight +
                        extraBottom

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding())
                    ) {
                        CompositionLocalProvider(
                            LocalNativeBottomOverlayPadding provides bottomPadding
                        ) {
                            IosScreenContent(route, navController)
                        }
                    }
                }
            }
        }
    }
}
