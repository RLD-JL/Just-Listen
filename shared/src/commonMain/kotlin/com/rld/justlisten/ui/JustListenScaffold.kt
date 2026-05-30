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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
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
    val showBottomBar =
        !routeLabel.contains("PlaylistDetail") && !routeLabel.contains("AddPlaylist")

    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberJustListenScaffoldState(repository, musicPlayer, coroutineScope)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxHeight = this.maxHeight
        val minibarHeight = 65.dp
        val bottomNavHeight = 56.dp
        val density = LocalDensity.current

        // endAnchor = the Y offset at which the minibar's TOP edge sits
        // so that its BOTTOM edge is flush with the top of the nav bar.
        //
        // Total screen height
        //   minus nav bar height   (so minibar bottom = nav bar top)
        //   minus minibar height   (so minibar top is above its bottom)
        //
        // When showBottomBar is false there's no nav bar, so just leave
        // room for the minibar above the very bottom of the screen.
        val endAnchor = with(density) {
            (maxHeight
                    - (if (showBottomBar) bottomNavHeight else 0.dp)
                    - minibarHeight
                    ).toPx()
        }
        val startAnchor = 0f

        val decayAnimationSpec = rememberSplineBasedDecay<Float>()

        val anchoredDraggableState = remember(endAnchor) {
            AnchoredDraggableState(
                initialValue = PlayBarState.COLLAPSED,
                anchors = DraggableAnchors {
                    PlayBarState.EXPANDED at startAnchor
                    PlayBarState.COLLAPSED at endAnchor
                },
                positionalThreshold = { distance: Float -> distance * 0.3f },
                velocityThreshold = { with(density) { 100.dp.toPx() } },
                snapAnimationSpec = spring(stiffness = 300f, dampingRatio = 0.8f),
                decayAnimationSpec = decayAnimationSpec
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

            // ── Main scaffold with nav bar ───────────────────────────────────
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (showBottomBar) {
                        Level1BottomBar(
                            navController = navController,
                            showDonationTab = showDonationTab,
                            modifier = Modifier.offset {
                                // Slide the nav bar down as the player fully expands
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
                },
            ) { innerPadding ->
                // innerPadding.calculateBottomPadding() already includes the
                // nav bar height. We add minibarHeight on top so content
                // isn't hidden behind the minibar.
                val extraBottom = if (shouldShowPlayBar) minibarHeight else 0.dp
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(
                            top    = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding() + extraBottom,
                        )
                ) {
                    AppNavigation(navController = navController)
                }
            }

            // ── Mini / full player ───────────────────────────────────────────
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
                        newDominantColor = {},
                        playBarMinimizedClicked = {
                            coroutineScope.launch {
                                anchoredDraggableState.animateTo(PlayBarState.EXPANDED)
                            }
                        },
                    )
                }
            }
        }
    }
}