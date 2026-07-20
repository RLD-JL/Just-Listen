package com.rld.justlisten.navigation

import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.ui.JustListenTheme
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.ui.PlayBarState
import com.rld.justlisten.ui.actions.PlayerAction
import com.rld.justlisten.ui.bottombars.playbar.PlayerBarSheetContent
import com.rld.justlisten.ui.bottombars.playbar.PlayerLayoutInfo
import com.rld.justlisten.ui.bottombars.playbar.PlayerUiEvent
import com.rld.justlisten.viewmodel.player.PlayerViewModel
import com.rld.justlisten.viewmodel.settings.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import platform.UIKit.UIViewController

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
fun PlayerViewController(
    initialExpanded: Boolean = false,
    onNavigate: (Route) -> Unit,
    onHeightChanged: (Boolean) -> Unit,
    onVisibilityChanged: (Boolean) -> Unit
): UIViewController = ComposeUIViewController(
    configure = {
        parallelRendering = true
        opaque = false
    }
) {
    val musicPlayer: MusicPlayer = koinInject()
    val viewModel: PlayerViewModel = koinViewModel()
    val viewModelUiState by viewModel.playerUiState.collectAsState()
    val livePlaybackState by musicPlayer.playbackState.collectAsState()
    val uiState = viewModelUiState.copy(playbackState = livePlaybackState)
    val settingsState by koinInject<SettingsViewModel>().settingsState.collectAsState()

    val playbackState = livePlaybackState
    val shouldShowPlayBar = playbackState.status == PlaybackStatus.PLAYING ||
        playbackState.status == PlaybackStatus.PAUSED ||
        playbackState.status == PlaybackStatus.BUFFERING ||
        playbackState.currentMedia != null

    LaunchedEffect(shouldShowPlayBar) {
        onVisibilityChanged(shouldShowPlayBar)
    }

    val customColors = com.rld.justlisten.ui.theme.CustomThemeColors(
        primary = settingsState.customPrimary,
        secondary = settingsState.customSecondary,
        background = settingsState.customBackground,
        surface = settingsState.customSurface,
    )

    CompositionLocalProvider(LocalMusicPlayer provides musicPlayer) {
        JustListenTheme(
            darkTheme = settingsState.isDarkThemeOn,
            palletColor = settingsState.palletColor,
            customColors = customColors
        ) {
            if (shouldShowPlayBar) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val maxHeight = this.maxHeight
                    val minibarHeight = IosPlayerLayoutMetrics.miniPlayerHeight
                    val density = LocalDensity.current
                    val coroutineScope = rememberCoroutineScope()

                    val anchoredDraggableState = remember {
                        AnchoredDraggableState(
                            initialValue = if (initialExpanded) {
                                PlayBarState.EXPANDED
                            } else {
                                PlayBarState.COLLAPSED
                            }
                        )
                    }

                    val bottomSafeArea =
                        if (anchoredDraggableState.targetValue == PlayBarState.EXPANDED) {
                            WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
                        } else {
                            0.dp
                        }

                    val windowSafeArea =
                        WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
                    val endAnchor = with(density) {
                        val calculated =
                            (maxHeight - windowSafeArea - minibarHeight -
                                IosPlayerLayoutMetrics.tabBarContentHeight).toPx()
                        if (calculated <= 0f) 1f else calculated
                    }
                    val startAnchor = 0f

                    SideEffect {
                        anchoredDraggableState.updateAnchors(
                            DraggableAnchors {
                                PlayBarState.EXPANDED at startAnchor
                                PlayBarState.COLLAPSED at endAnchor
                            }
                        )
                    }

                    val isExpanded =
                        anchoredDraggableState.currentValue == PlayBarState.EXPANDED
                    LaunchedEffect(isExpanded) {
                        onHeightChanged(isExpanded)
                    }

                    val currentFractionState = remember(endAnchor) {
                        derivedStateOf {
                            val fullRange = endAnchor - startAnchor
                            val currentOffset = anchoredDraggableState.offset
                            if (fullRange == 0f || currentOffset.isNaN()) {
                                0f
                            } else {
                                ((endAnchor - currentOffset) / fullRange).coerceIn(0f, 1f)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset {
                                val offset = anchoredDraggableState.offset
                                IntOffset(
                                    x = 0,
                                    y = if (offset.isNaN()) endAnchor.toInt() else offset.toInt()
                                )
                            }
                            .anchoredDraggable(
                                state = anchoredDraggableState,
                                orientation = Orientation.Vertical,
                                flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                                    state = anchoredDraggableState,
                                    positionalThreshold = { distance: Float -> distance * 0.3f },
                                    animationSpec = spring(
                                        stiffness = 300f,
                                        dampingRatio = 0.8f
                                    )
                                )
                            )
                    ) {
                        val isExtended =
                            anchoredDraggableState.currentValue == PlayBarState.EXPANDED
                        val layoutInfo = remember(bottomSafeArea, isExtended) {
                            PlayerLayoutInfo(
                                bottomPadding = bottomSafeArea,
                                currentFractionProvider = { currentFractionState.value },
                                isExtended = isExtended
                            )
                        }

                        PlayerBarSheetContent(
                            uiState = uiState,
                            layoutInfo = layoutInfo,
                            onAction = { action ->
                                if (action is PlayerAction.ConnectAudiusPressed) {
                                    viewModel.onAction(PlayerAction.DismissConnectPrompt)
                                    onNavigate(Route.Settings)
                                } else {
                                    viewModel.onAction(action)
                                }
                            },
                            onUiEvent = { uiEvent ->
                                when (uiEvent) {
                                    PlayerUiEvent.Collapse -> coroutineScope.launch {
                                        anchoredDraggableState.animateTo(PlayBarState.COLLAPSED)
                                    }

                                    PlayerUiEvent.Expand -> coroutineScope.launch {
                                        anchoredDraggableState.animateTo(PlayBarState.EXPANDED)
                                    }

                                    is PlayerUiEvent.NavigateToArtist -> {
                                        coroutineScope.launch {
                                            anchoredDraggableState.animateTo(PlayBarState.COLLAPSED)
                                        }
                                        onNavigate(
                                            Route.ArtistProfile(
                                                uiEvent.artistId,
                                                uiEvent.artistName
                                            )
                                        )
                                    }

                                    else -> Unit
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
