package com.rld.justlisten.ui.seeallscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.rld.justlisten.ui.actions.SeeAllAction
import com.rld.justlisten.ui.components.AnimatedShimmer
import com.rld.justlisten.ui.components.MusicLoadingSpinner
import com.rld.justlisten.ui.components.MusicLoadingScreen
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.ui.theme.typography
import com.rld.justlisten.viewmodel.interfaces.Item
import com.rld.justlisten.viewmodel.screens.playlist.TimeRange
import com.rld.justlisten.viewmodel.screens.playlist.TracksCategory
import com.rld.justlisten.viewmodel.screens.playlist.getTrackCategory
import com.rld.justlisten.viewmodel.seeall.SeeAllState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeeAllScreen(
    seeAllState: SeeAllState,
    onAction: (SeeAllAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = seeAllState.title,
                        style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(SeeAllAction.BackPressed) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Dynamic Themed Genre Filter Pills (Only for See All Tracks) - Genre on Top
            if (seeAllState.playlistEnum == "TRACKS") {
                val genres = getTrackCategory()
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(genres) { genre ->
                        val isSelected = genre.value == seeAllState.queryPlaylist
                        val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
                        val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(containerColor)
                                .clickable {
                                    onAction(SeeAllAction.ChangeGenre(genre))
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = genre.value,
                                style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = contentColor
                            )
                        }
                    }
                }
            }

            // 1b. Time Filters Row ("All", "This Week", "This Month") - Period Under
            if (seeAllState.playlistEnum == "TRACKS") {
                val timeRanges = listOf(TimeRange.ALLTIME, TimeRange.WEEK, TimeRange.MONTH)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    items(timeRanges) { item ->
                        val isSelected = item == seeAllState.selectedTimeRange
                        val label = when (item) {
                            TimeRange.ALLTIME -> "All"
                            TimeRange.WEEK -> "This Week"
                            TimeRange.MONTH -> "This Month"
                        }
                        val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
                        val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(containerColor)
                                .clickable {
                                    onAction(SeeAllAction.ChangeTimeRange(item))
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = contentColor
                            )
                        }
                    }
                }
            }

            // 2. Vertical list of playlists/items
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (seeAllState.isLoading && seeAllState.items.isEmpty()) {
                    MusicLoadingScreen(showText = true)
                } else if (seeAllState.items.isEmpty()) {
                    Text(
                        text = "No items found",
                        style = typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val listState = rememberLazyListState()
                    val shouldLoadMore = remember {
                        derivedStateOf {
                            val totalItemsCount = listState.layoutInfo.totalItemsCount
                            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 2
                        }
                    }
                    
                    LaunchedEffect(shouldLoadMore.value) {
                        if (shouldLoadMore.value && !seeAllState.lastItemReached && !seeAllState.isLoading) {
                            onAction(SeeAllAction.LoadMore(seeAllState.items.size + 20))
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(seeAllState.items) { item ->
                            SeeAllListItem(
                                item = item,
                                onClick = {
                                    if (seeAllState.playlistEnum == "TRACKS") {
                                        onAction(
                                            SeeAllAction.SongPressed(
                                                songId = item.id,
                                                title = item.playlistTitle,
                                                user = item.user,
                                                songIconList = item.songIconList
                                            )
                                        )
                                    } else {
                                        onAction(
                                            SeeAllAction.PlaylistClicked(
                                                playlistId = item.id,
                                                playlistIcon = item.songIconList.songImageURL480px,
                                                createdBy = item.user,
                                                title = item.playlistTitle
                                            )
                                        )
                                    }
                                }
                            )
                        }
                        
                        if (seeAllState.isLoading) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(88.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MusicLoadingScreen(
                                            size = 64.dp,
                                            showText = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeeAllListItem(
    item: Item,
    onClick: () -> Unit
) {
    val musicPlayer = LocalMusicPlayer.current
    val playbackState by musicPlayer.playbackState.collectAsState()
    val isPlayingThisItem = playbackState.status == PlaybackStatus.PLAYING &&
            (playbackState.currentMedia?.id == item.id || musicPlayer.currentlyPlayingPlaylistId == item.id)

    val context = LocalPlatformContext.current
    val painter = rememberAsyncImagePainter(
        model = remember(item.songIconList.songImageURL480px, context) {
            ImageRequest.Builder(context)
                .data(item.songIconList.songImageURL480px)
                .allowHardware(true)
                .build()
        }
    )
    val state by painter.state.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (state is AsyncImagePainter.State.Loading) {
                    AnimatedShimmer(64.dp, 64.dp)
                }
                if (isPlayingThisItem) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        MusicLoadingSpinner(
                            size = 20.dp,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.playlistTitle,
                    style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.user,
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // Repost and Favorite counters with Dynamic themed icons (No heart button on the right)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reposts",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatCount(item.repostCount),
                            style = typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorites",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatCount(item.favoriteCount),
                            style = typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${(count / 100_000).toFloat() / 10}M"
        count >= 1_000 -> "${(count / 100).toFloat() / 10}K"
        else -> count.toString()
    }
}
