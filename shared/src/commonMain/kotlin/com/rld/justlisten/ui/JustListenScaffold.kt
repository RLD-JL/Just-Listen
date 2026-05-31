package com.rld.justlisten.ui

import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.navigation.AppNavigation
import com.rld.justlisten.ui.bottombars.bottombarnav.Level1BottomBar
import com.rld.justlisten.ui.bottombars.playbar.PlayerBarSheetContent
import kotlinx.coroutines.launch

enum class PlayBarState {
    COLLAPSED,
    EXPANDED
}

@OptIn(ExperimentalFoundationApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun JustListenScaffold(
    navController: NavHostController,
    musicPlayer: MusicPlayer,
    showDonationTab: Boolean,
    modifier: Modifier = Modifier,
    repository: Repository,
) {
    val playbackState by musicPlayer.playbackState.collectAsState()
    val shouldShowPlayBar = playbackState.status == PlaybackStatus.PLAYING ||
            playbackState.status == PlaybackStatus.PAUSED ||
            playbackState.status == PlaybackStatus.BUFFERING ||
            playbackState.currentMedia != null

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val routeLabel = navBackStackEntry?.destination?.route.orEmpty()
    val showBottomBar = !routeLabel.contains("AddPlaylist")

    val primaryThemeColor = androidx.compose.material3.MaterialTheme.colorScheme.primary

    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberJustListenScaffoldState(repository, musicPlayer, coroutineScope)

    var dominantColor by remember { mutableStateOf(Color.Transparent) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxHeight = this.maxHeight
        val minibarHeight = 65.dp
        val bottomNavHeight = 80.dp
        val density = LocalDensity.current

        // Calculate endAnchor based on whether the nav bar is showing
        val endAnchor = with(density) {
            (maxHeight
                    - (if (showBottomBar) bottomNavHeight else 0.dp)
                    - minibarHeight
                    ).toPx()
        }
        val startAnchor = 0f

        val decayAnimationSpec = rememberSplineBasedDecay<Float>()

        // FIX 1: Removed `endAnchor` from remember keys so state survives navigation
        val anchoredDraggableState = remember {
            AnchoredDraggableState(
                initialValue = PlayBarState.COLLAPSED,
                positionalThreshold = { distance: Float -> distance * 0.3f },
                velocityThreshold = { with(density) { 100.dp.toPx() } },
                snapAnimationSpec = spring(stiffness = 300f, dampingRatio = 0.8f),
                decayAnimationSpec = decayAnimationSpec
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

        val currentFraction by remember {
            derivedStateOf {
                val fullRange = endAnchor - startAnchor
                val currentOffset = anchoredDraggableState.offset
                if (fullRange == 0f || currentOffset.isNaN()) 0f
                else ((endAnchor - currentOffset) / fullRange).coerceIn(0f, 1f)
            }
        }

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
                    AppNavigation(navController = navController)
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
                            orientation = Orientation.Vertical
                        )
                ) {
                    PlayerBarSheetContent(
                        bottomPadding = if (showBottomBar) bottomNavHeight else 0.dp,
                        currentFraction = currentFraction,
                        isExtended = anchoredDraggableState.currentValue == PlayBarState.EXPANDED,
                        onSkipNextPressed = { musicPlayer.skipToNext() },
                        musicPlayer = musicPlayer,
                        onCollapsedClicked = {
                            coroutineScope.launch {
                                anchoredDraggableState.animateTo(PlayBarState.COLLAPSED)
                            }
                        },
                        onFavoritePressed = { id, title, user, songIcon, isFavorite ->
                            scaffoldState.saveSongToFavorites(
                                id, title, user, songIcon, isFavorite
                            )
                        },
                        addPlaylistList = scaffoldState.addPlaylistList,
                        onAddPlaylistClicked = { name, description ->
                            scaffoldState.savePlaylist(name, description)
                        },
                        getLatestPlaylist = {
                            scaffoldState.loadAddPlaylists()
                        },
                        clickedToAddSongToPlaylist = { title, description, songs ->
                            scaffoldState.updatePlaylistSongs(title, description, songs)
                        },
                        newDominantColor = { colorInt ->
                            // Blend the extracted color with the theme's primary color
                            val extracted = Color(colorInt)
                            val blended = androidx.compose.ui.graphics.lerp(extracted, primaryThemeColor, 0.6f)
                            dominantColor = blended.copy(alpha = 0.3f)
                        },
                        playBarMinimizedClicked = {
                            coroutineScope.launch {
                                anchoredDraggableState.animateTo(PlayBarState.EXPANDED)
                            }
                        },
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
                            val progress = if (currentFraction > 0.8f)
                                (currentFraction - 0.8f) / 0.2f
                            else 0f
                            IntOffset(
                                x = 0,
                                y = (bottomNavHeight.toPx() * progress).toInt()
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