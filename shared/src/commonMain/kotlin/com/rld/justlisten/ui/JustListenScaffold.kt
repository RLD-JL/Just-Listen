package com.rld.justlisten.ui

import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.koin.compose.viewmodel.koinViewModel
import com.rld.justlisten.viewmodel.player.PlayerViewModel
import com.rld.justlisten.ui.actions.PlayerAction
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.navigation.AppNavigation
import com.rld.justlisten.ui.bottombars.bottombarnav.Level1BottomBar
import com.rld.justlisten.ui.bottombars.playbar.PlayerBarSheetContent
import com.rld.justlisten.ui.bottombars.playbar.PlayerLayoutInfo
import com.rld.justlisten.ui.bottombars.playbar.PlayerUiEvent
import kotlinx.coroutines.launch
import com.rld.justlisten.navigation.Route

enum class PlayBarState {
    COLLAPSED,
    EXPANDED
}

@OptIn(ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun JustListenScaffold(
    navController: NavHostController,
    showDonationTab: Boolean,
    startDestination: Route = Route.Playlist,
    modifier: Modifier = Modifier,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val routeLabel = navBackStackEntry?.destination?.route.orEmpty()

    val viewModel: PlayerViewModel = koinViewModel()
    val uiState by viewModel.playerUiState.collectAsState()
    
    val playbackState = uiState.playbackState ?: com.rld.justlisten.media.PlaybackState(
        status = PlaybackStatus.IDLE,
        currentPosition = 0
    )
    val isKeyboardVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 20.dp
    val shouldShowPlayBar = (playbackState.status == PlaybackStatus.PLAYING ||
            playbackState.status == PlaybackStatus.PAUSED ||
            playbackState.status == PlaybackStatus.BUFFERING ||
            playbackState.currentMedia != null) && !isKeyboardVisible && !routeLabel.contains("Onboarding")

    val showBottomBar = !routeLabel.contains("AddPlaylist") && !routeLabel.contains("Onboarding") && !isKeyboardVisible

    val primaryThemeColor = MaterialTheme.colorScheme.primary

    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxHeight = this.maxHeight
        val minibarHeight = 65.dp
        val bottomNavHeight = 80.dp
        val density = LocalDensity.current
        val bottomSafeArea = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()

        // Calculate endAnchor based on whether the nav bar is showing
        val endAnchor = with(density) {
            (maxHeight
                    - (if (showBottomBar) bottomNavHeight + bottomSafeArea else bottomSafeArea)
                    - minibarHeight
                    ).toPx()
        }
        val startAnchor = 0f

        val decayAnimationSpec = rememberSplineBasedDecay<Float>()

        // FIX 1: Removed `endAnchor` from remember keys so state survives navigation
        val anchoredDraggableState = remember {
            AnchoredDraggableState(
                initialValue = PlayBarState.COLLAPSED
            )
        }

        // FIX 2: Smoothly update the anchors whenever endAnchor changes
        SideEffect {
            anchoredDraggableState.updateAnchors(
                DraggableAnchors {
                    PlayBarState.EXPANDED at startAnchor
                    PlayBarState.COLLAPSED at endAnchor
                }
            )
        }

        val currentFractionState = remember(endAnchor) {
            derivedStateOf {
                val fullRange = endAnchor - startAnchor
                val currentOffset = anchoredDraggableState.offset
                if (fullRange == 0f || currentOffset.isNaN()) 0f
                else ((endAnchor - currentOffset) / fullRange).coerceIn(0f, 1f)
            }
        }

        val themeBg = MaterialTheme.colorScheme.background
        val isDarkTheme = (themeBg.red * 0.299f + themeBg.green * 0.587f + themeBg.blue * 0.114f) < 0.5f

        SystemBarsColorController(
            currentFractionProvider = { currentFractionState.value },
            themeBg = themeBg,
            isDarkTheme = isDarkTheme
        )

        Box(modifier = Modifier.fillMaxSize()) {

            // 1. ── Main scaffold (Without bottomBar passed into it) ──────────
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent, // Let the Box's background show through
            ) { innerPadding ->

                // Calculate padding manually to account for dynamically showing/hiding items
                val navBarPadding = if (showBottomBar) bottomNavHeight else 0.dp
                val extraBottom = if (shouldShowPlayBar) minibarHeight else 0.dp

                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(
                            top    = innerPadding.calculateTopPadding(),
                            // Combine all paddings together to prevent overlapping
                            bottom = innerPadding.calculateBottomPadding() + navBarPadding + extraBottom,
                        )
                ) {
                    AppNavigation(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }

            // 2. ── Mini / full player ────────────────────────────────────────
            if (shouldShowPlayBar) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset {
                            val offset = anchoredDraggableState.offset
                            IntOffset(
                                x = 0,
                                y = if (offset.isNaN()) endAnchor.toInt()
                                else offset.toInt()
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
                    PlayerSheetWrapper(
                        uiState = uiState,
                        anchoredDraggableState = anchoredDraggableState,
                        showBottomBar = showBottomBar,
                        bottomNavHeight = bottomNavHeight,
                        bottomSafeArea = bottomSafeArea,
                        currentFractionProvider = { currentFractionState.value },
                        onAction = { action ->
                            if (action is PlayerAction.ConnectAudiusPressed) {
                                viewModel.onAction(PlayerAction.DismissConnectPrompt)
                                navController.navigate(Route.Settings)
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
                                    navController.navigate(Route.ArtistProfile(uiEvent.artistId, uiEvent.artistName))
                                }
                                else -> Unit
                            }
                        }
                    )
                }
            }

            // 3. ── Bottom Navigation (Drawn LAST -> Highest Z-Index) ─────────
            // FIX 3: Placed at the root Box level so it perfectly overlaps the player
            if (showBottomBar) {
                Level1BottomBar(
                    navController = navController,
                    showDonationTab = showDonationTab,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset {
                            // Slide the nav bar down out of frame as the player fully expands
                            val fraction = currentFractionState.value
                            val progress = if (fraction > 0.8f)
                                (fraction - 0.8f) / 0.2f
                            else 0f
                            IntOffset(
                                x = 0,
                                y = ((bottomNavHeight + bottomSafeArea).toPx() * progress).toInt()
                            )
                        },
                    onItemClick = {
                        coroutineScope.launch {
                            anchoredDraggableState.animateTo(PlayBarState.COLLAPSED)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SystemBarsColorController(
    currentFractionProvider: () -> Float,
    themeBg: Color,
    isDarkTheme: Boolean
) {
    val currentFraction = currentFractionProvider()
    val statusBarColor = androidx.compose.ui.graphics.lerp(themeBg, Color.Black, currentFraction)
    val navigationBarColor = androidx.compose.ui.graphics.lerp(themeBg, Color.Black, currentFraction)
    val darkIcons = if (currentFraction > 0.5f) false else !isDarkTheme

    SetSystemBarsColor(
        statusBarColor = statusBarColor,
        navigationBarColor = navigationBarColor,
        darkIcons = darkIcons
    )
}

@OptIn(ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun PlayerSheetWrapper(
    uiState: com.rld.justlisten.viewmodel.player.PlayerUiState,
    anchoredDraggableState: AnchoredDraggableState<PlayBarState>,
    showBottomBar: Boolean,
    bottomNavHeight: androidx.compose.ui.unit.Dp,
    bottomSafeArea: androidx.compose.ui.unit.Dp,
    currentFractionProvider: () -> Float,
    onAction: (PlayerAction) -> Unit,
    onUiEvent: (PlayerUiEvent) -> Unit
) {
    val isExtended = anchoredDraggableState.currentValue == PlayBarState.EXPANDED
    val layoutInfo = remember(showBottomBar, bottomNavHeight, bottomSafeArea, isExtended) {
        PlayerLayoutInfo(
            bottomPadding = (if (showBottomBar) bottomNavHeight else 0.dp) + bottomSafeArea,
            currentFractionProvider = currentFractionProvider,
            isExtended = isExtended
        )
    }

    PlayerBarSheetContent(
        uiState = uiState,
        layoutInfo = layoutInfo,
        onAction = onAction,
        onUiEvent = onUiEvent
    )
}