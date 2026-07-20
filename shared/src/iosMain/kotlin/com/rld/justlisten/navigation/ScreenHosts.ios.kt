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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.compose.rememberNavController
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.JustListenApp
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.ui.JustListenTheme
import com.rld.justlisten.ui.PlayBarState
import com.rld.justlisten.ui.actions.PlayerAction
import com.rld.justlisten.ui.bottombars.playbar.PlayerBarSheetContent
import com.rld.justlisten.ui.bottombars.playbar.PlayerLayoutInfo
import com.rld.justlisten.ui.bottombars.playbar.PlayerUiEvent
import com.rld.justlisten.viewmodel.player.PlayerViewModel
import com.rld.justlisten.viewmodel.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import platform.UIKit.UIColor
import platform.UIKit.UIViewController

// Existing pre-iOS 26 entry point
@OptIn(ExperimentalComposeUiApi::class)
fun MainViewController(): UIViewController = ComposeUIViewController(
    configure = { parallelRendering = true }
) {
    JustListenApp()
}

// Single Screen entry point for individual routes on iOS
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
                    val playbackState = playerUiState.playbackState ?: com.rld.justlisten.media.PlaybackState(
                        status = com.rld.justlisten.media.PlaybackStatus.IDLE,
                        currentPosition = 0
                    )
                    val shouldShowPlayBar = (playbackState.status == com.rld.justlisten.media.PlaybackStatus.PLAYING ||
                            playbackState.status == com.rld.justlisten.media.PlaybackStatus.PAUSED ||
                            playbackState.status == com.rld.justlisten.media.PlaybackStatus.BUFFERING ||
                            playbackState.currentMedia != null)

                    val navBarPadding = 49.dp
                    val extraBottom = if (shouldShowPlayBar) 65.dp else 0.dp
                    val bottomPadding = bottomSafeArea.dp + navBarPadding + extraBottom

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

data class IosMiniTrackState(
    val id: String,
    val title: String,
    val artist: String,
    val artworkUrl: String,
)

data class IosMiniPlayerState(
    val visible: Boolean,
    val currentTrack: IosMiniTrackState?,
    val previousTrack: IosMiniTrackState?,
    val nextTrack: IosMiniTrackState?,
    val isPlaying: Boolean,
    val isBuffering: Boolean,
)

private fun com.rld.justlisten.media.MediaMetadata.toIosMiniTrackState() = IosMiniTrackState(
    id = id,
    title = title,
    artist = artist,
    artworkUrl = lowResArtworkUrl?.takeIf { it.isNotBlank() }
        ?: artworkUrl.orEmpty(),
)

private fun com.rld.justlisten.media.PlaybackState.toIosMiniPlayerState(
    playlist: List<com.rld.justlisten.media.MediaMetadata>,
): IosMiniPlayerState {
    val media = currentMedia
    val visible = status == com.rld.justlisten.media.PlaybackStatus.PLAYING ||
        status == com.rld.justlisten.media.PlaybackStatus.PAUSED ||
        status == com.rld.justlisten.media.PlaybackStatus.BUFFERING ||
        media != null
    val currentIndex = playlist.indexOfFirst { it.id == media?.id }
    val previous = when {
        currentIndex > 0 -> playlist.getOrNull(currentIndex - 1)
        currentIndex == 0 && repeatMode == com.rld.justlisten.media.RepeatMode.ALL -> playlist.lastOrNull()
        else -> null
    }
    val next = when {
        currentIndex in 0 until playlist.lastIndex -> playlist.getOrNull(currentIndex + 1)
        currentIndex == playlist.lastIndex && repeatMode == com.rld.justlisten.media.RepeatMode.ALL -> playlist.firstOrNull()
        else -> null
    }

    return IosMiniPlayerState(
        visible = visible,
        currentTrack = media?.toIosMiniTrackState(),
        previousTrack = previous?.toIosMiniTrackState(),
        nextTrack = next?.toIosMiniTrackState(),
        isPlaying = status == com.rld.justlisten.media.PlaybackStatus.PLAYING,
        isBuffering = status == com.rld.justlisten.media.PlaybackStatus.BUFFERING,
    )
}

private class IosPlayerStateObserverController(
    private val onStateChanged: (IosMiniPlayerState) -> Unit,
) : UIViewController(nibName = null, bundle = null) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.clearColor

        val musicPlayer = org.koin.mp.KoinPlatform.getKoin().get<MusicPlayer>()
        scope.launch {
            combine(musicPlayer.playbackState, musicPlayer.currentPlaylist) { playbackState, playlist ->
                playbackState.toIosMiniPlayerState(playlist)
            }
                .distinctUntilChanged()
                .collect { onStateChanged(it) }
        }
    }
}

// A plain UIKit observer deliberately avoids creating another Compose/Metal
// surface alongside the selected tab's ComposeUIViewController.
fun PlayerStateObserverViewController(
    onStateChanged: (IosMiniPlayerState) -> Unit,
): UIViewController = IosPlayerStateObserverController(onStateChanged)

fun iosPlayerTogglePlayback() {
    val player = org.koin.mp.KoinPlatform.getKoin().get<MusicPlayer>()
    if (player.playbackState.value.status == com.rld.justlisten.media.PlaybackStatus.PLAYING) {
        player.pause()
    } else {
        player.play()
    }
}

fun iosPlayerSkipToNext() {
    org.koin.mp.KoinPlatform.getKoin().get<MusicPlayer>().skipToNext()
}

fun iosPlayerSkipToPrevious() {
    org.koin.mp.KoinPlatform.getKoin().get<MusicPlayer>().skipToPrevious()
}

// Global Player entry point for iOS
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
    val shouldShowPlayBar = (playbackState.status == com.rld.justlisten.media.PlaybackStatus.PLAYING ||
            playbackState.status == com.rld.justlisten.media.PlaybackStatus.PAUSED ||
            playbackState.status == com.rld.justlisten.media.PlaybackStatus.BUFFERING ||
            playbackState.currentMedia != null)

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
                val minibarHeight = 65.dp
                val density = LocalDensity.current
                val coroutineScope = rememberCoroutineScope()

                val anchoredDraggableState = remember {
                    AnchoredDraggableState(initialValue = if (initialExpanded) PlayBarState.EXPANDED else PlayBarState.COLLAPSED)
                }

                val bottomSafeArea = if (anchoredDraggableState.targetValue == PlayBarState.EXPANDED) {
                    WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
                } else {
                    0.dp
                }

                val windowSafeArea = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
                val endAnchor = with(density) {
                    val calculated = (maxHeight - windowSafeArea - minibarHeight - 49.dp).toPx()
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

                val isExpanded = anchoredDraggableState.currentValue == PlayBarState.EXPANDED
                LaunchedEffect(isExpanded) {
                    onHeightChanged(isExpanded)
                }

                val currentFractionState = remember(endAnchor) {
                    derivedStateOf {
                        val fullRange = endAnchor - startAnchor
                        val currentOffset = anchoredDraggableState.offset
                        if (fullRange == 0f || currentOffset.isNaN()) 0f
                        else ((endAnchor - currentOffset) / fullRange).coerceIn(0f, 1f)
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
                                animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f)
                            )
                        )
                ) {
                    val isExtended = anchoredDraggableState.currentValue == PlayBarState.EXPANDED
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
                                PlayerUiEvent.Collapse -> {
                                    coroutineScope.launch {
                                        anchoredDraggableState.animateTo(PlayBarState.COLLAPSED)
                                    }
                                }
                                PlayerUiEvent.Expand -> {
                                    coroutineScope.launch {
                                        anchoredDraggableState.animateTo(PlayBarState.EXPANDED)
                                    }
                                }
                                is PlayerUiEvent.NavigateToArtist -> {
                                    coroutineScope.launch {
                                        anchoredDraggableState.animateTo(PlayBarState.COLLAPSED)
                                    }
                                    onNavigate(Route.ArtistProfile(uiEvent.artistId, uiEvent.artistName))
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
