package com.rld.justlisten.ui.feedscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.rld.justlisten.ui.components.MusicLoadingSpinner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.ui.actions.FeedAction
import com.rld.justlisten.ui.artistprofile.components.ConnectPromptDialog
import com.rld.justlisten.ui.components.AnimatedShimmer
import com.rld.justlisten.ui.components.MusicLoadingScreen
import com.rld.justlisten.ui.seeallscreen.formatCount
import com.rld.justlisten.ui.theme.typography
import com.rld.justlisten.viewmodel.feed.FeedState
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.feed.FeedTab
import com.rld.justlisten.viewmodel.feed.FeedFilter
import com.rld.justlisten.viewmodel.feed.FeedFormat
import com.rld.justlisten.viewmodel.screens.playlist.getTrackCategory
import com.rld.justlisten.viewmodel.screens.playlist.getTimeRange
import com.rld.justlisten.viewmodel.screens.playlist.TimeRange
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import org.jetbrains.compose.resources.painterResource
import justlisten.shared.generated.resources.Res
import justlisten.shared.generated.resources.ic_repost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    feedState: FeedState,
    musicPlayer: MusicPlayer,
    libraryRepository: LibraryRepository,
    onAction: (FeedAction) -> Unit
) {
    val playbackState by musicPlayer.playbackState.collectAsState()
    val currentPlayingSongId = playbackState.currentMedia?.id
    val isPlaying = playbackState.status == PlaybackStatus.PLAYING
    if (feedState.showConnectPrompt) {
        ConnectPromptDialog(
            onDismissRequest = { onAction(FeedAction.DismissConnectPrompt) },
            onConnectClick = { onAction(FeedAction.ConnectAudiusPressed) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Music Feed",
                        style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    // Pull to refresh is used instead of a corner button
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
            // Tab Selection (Following vs Trending)
            TabRow(
                selectedTabIndex = feedState.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)) }
            ) {
                Tab(
                    selected = feedState.selectedTab == FeedTab.FOLLOWING,
                    onClick = { onAction(FeedAction.SelectTab(FeedTab.FOLLOWING)) },
                    text = { Text("Following", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = feedState.selectedTab == FeedTab.TRENDING,
                    onClick = { onAction(FeedAction.SelectTab(FeedTab.TRENDING)) },
                    text = { Text("Trending", fontWeight = FontWeight.Bold) }
                )
            }

            // Filter Chips Bar
            if (feedState.selectedTab == FeedTab.FOLLOWING && !feedState.isGuest) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Row 1: All/Originals/Reposts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FeedFilter.values().forEach { filter ->
                            val isSelected = feedState.personalFilter == filter
                            val label = when (filter) {
                                FeedFilter.ALL -> "All Feed"
                                FeedFilter.ORIGINAL -> "Originals"
                                FeedFilter.REPOST -> "Reposts"
                            }
                            FilterPill(
                                text = label,
                                isSelected = isSelected,
                                onClick = { onAction(FeedAction.SetPersonalFilter(filter)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: All/Tracks/Playlists/Albums
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(FeedFormat.values().toList(), key = { it.name }) { format ->
                            val isSelected = feedState.personalFormat == format
                            val label = when (format) {
                                FeedFormat.ALL -> "All Formats"
                                FeedFormat.TRACKS -> "Tracks"
                                FeedFormat.PLAYLISTS -> "Playlists"
                                FeedFormat.ALBUMS -> "Albums"
                            }
                            FilterPill(
                                text = label,
                                isSelected = isSelected,
                                onClick = { onAction(FeedAction.SetPersonalFormat(format)) }
                            )
                        }
                    }
                }
            } else if (feedState.selectedTab == FeedTab.TRENDING) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Row 1: Genres List
                    val genres = getTrackCategory()
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(genres, key = { it.name }) { genre ->
                            val isSelected = feedState.trendingCategory == genre
                            FilterPill(
                                text = genre.value,
                                isSelected = isSelected,
                                onClick = { onAction(FeedAction.SetTrendingCategory(genre)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Time Frame
                    val timeRanges = getTimeRange()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        timeRanges.forEach { range ->
                            val isSelected = feedState.trendingTimeRange == range
                            val label = when (range) {
                                TimeRange.WEEK -> "This Week"
                                TimeRange.MONTH -> "This Month"
                                TimeRange.ALLTIME -> "All Time"
                            }
                            FilterPill(
                                text = label,
                                isSelected = isSelected,
                                onClick = { onAction(FeedAction.SetTrendingTimeRange(range)) }
                            )
                        }
                    }
                }
            }

            // Main Content Area with Pull-to-refresh
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (feedState.selectedTab == FeedTab.FOLLOWING && feedState.isGuest) {
                    GuestFeedPrompt(onConnectClick = { onAction(FeedAction.ConnectAudiusPressed) })
                } else {
                    val pullState = rememberPullToRefreshState()
                    PullToRefreshBox(
                        isRefreshing = feedState.isRefreshing,
                        onRefresh = { onAction(FeedAction.Refresh) },
                        state = pullState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (feedState.isLoading && feedState.items.isEmpty()) {
                            MusicLoadingScreen(showText = true)
                        } else if (feedState.items.isEmpty()) {
                            EmptyFeedScreen()
                        } else {
                            val listState = rememberLazyListState()
                            val shouldLoadMore = remember {
                                derivedStateOf {
                                    val totalItemsCount = listState.layoutInfo.totalItemsCount
                                    val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                                    totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 2
                                }
                            }

                            LaunchedEffect(shouldLoadMore.value, feedState.isLoading, feedState.lastItemReached) {
                                if (shouldLoadMore.value && !feedState.lastItemReached && !feedState.isLoading) {
                                    onAction(FeedAction.LoadMore)
                                }
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                            ) {
                                itemsIndexed(feedState.items, key = { _, item -> item.id }) { index, item ->
                                    val isPlayingThisSong = isPlaying && item.id == currentPlayingSongId
                                    FeedTimelineRow(
                                        playlistItem = item,
                                        onAction = onAction,
                                        isLast = index == feedState.items.size - 1,
                                        feedState = feedState,
                                        isPlaying = isPlayingThisSong
                                    )
                                }
                                if (feedState.isLoading && feedState.items.isNotEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(80.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            MusicLoadingSpinner(size = 32.dp, color = MaterialTheme.colorScheme.primary)
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
}

@Composable
fun GuestFeedPrompt(onConnectClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your Personalized Feed",
            style = typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Connect your Audius account to view updates, releases, and reposts from the artists you follow.",
            style = typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onConnectClick,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(48.dp)
        ) {
            Text(text = "Connect Account", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmptyFeedScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MusicVideo,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Feed is empty",
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Follow artists to see their uploads and reposts in your personalized feed!",
            style = typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun FeedTimelineRow(
    playlistItem: PlaylistItem,
    onAction: (FeedAction) -> Unit,
    isLast: Boolean,
    feedState: FeedState,
    isPlaying: Boolean = false
) {
    val isPlaylist = playlistItem._data.isPlaylist
    val isRepostActivity = playlistItem.isReposted || 
            playlistItem._data.followeeReposts.isNotEmpty() ||
            (feedState.selectedTab == FeedTab.FOLLOWING && feedState.personalFilter == FeedFilter.REPOST)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Left side: Solid vertical connector timeline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(36.dp)
                // Let's constrain height dynamically by placing timeline components
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            // Action Badge
            val badgeTint = if (isRepostActivity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(badgeTint.copy(alpha = 0.15f))
                    .border(1.5.dp, badgeTint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isRepostActivity) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_repost),
                        contentDescription = null,
                        tint = badgeTint,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    val badgeIcon = if (isPlaylist) Icons.Default.QueueMusic else Icons.Default.PlayArrow
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = badgeTint,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (!isLast) {
                // Vertical Line
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(130.dp)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right side: Premium activity feed card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    if (playlistItem._data.isPlaylist) {
                        onAction(
                            FeedAction.PlaylistClicked(
                                playlistId = playlistItem.id,
                                playlistIcon = playlistItem.songIconList.songImageURL480px,
                                createdBy = playlistItem.user,
                                title = playlistItem.playlistTitle.ifBlank { playlistItem.title }
                            )
                        )
                    } else {
                        onAction(FeedAction.SongPressed(playlistItem.id))
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        ) {
            val context = LocalPlatformContext.current
            val imagePainter = rememberAsyncImagePainter(
                model = remember(playlistItem.songIconList.songImageURL480px, context) {
                    ImageRequest.Builder(context)
                        .data(playlistItem.songIconList.songImageURL480px)
                        .build()
                }
            )
            val imageState by imagePainter.state.collectAsState()

            Column(modifier = Modifier.padding(12.dp)) {
                // 1. Activity Header (Artist Name + activity detail on left / Date on top right)
                val formattedDate = remember(playlistItem.releaseDate) { formatDate(playlistItem.releaseDate) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val annotatedText = remember(playlistItem.user, isRepostActivity, isPlaylist, primaryColor) {
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor)) {
                                append(playlistItem.user)
                            }
                            append(" ")
                            append(if (isRepostActivity) "reposted" else "uploaded")
                            append(" ")
                            append(if (isPlaylist) "a playlist" else "a track")
                        }
                    }

                    val artistId = playlistItem._data.user.id
                    val artistClickAction = if (artistId.isNotBlank()) {
                        { onAction(FeedAction.ArtistClicked(artistId, playlistItem.user)) }
                    } else null

                    Text(
                        text = annotatedText,
                        style = typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (artistClickAction != null) {
                                    Modifier.clickable(onClick = artistClickAction)
                                } else {
                                    Modifier
                                }
                            )
                    )

                    if (formattedDate.isNotEmpty()) {
                        Text(
                            text = formattedDate,
                            style = typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Card body (Image + Title)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            painter = imagePainter,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (imageState is AsyncImagePainter.State.Loading) {
                            AnimatedShimmer(60.dp, 60.dp)
                        }
                        if (isPlaying) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                MusicLoadingSpinner(
                                    size = 18.dp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlistItem.playlistTitle.ifBlank { playlistItem.title },
                            style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "by ${playlistItem.user}",
                            style = typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Footer Action buttons (repost, favorite, comment)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Repost button
                    Row(
                        modifier = Modifier
                            .clickable {
                                onAction(
                                    FeedAction.RepostPressed(
                                        playlistItem.id,
                                        !playlistItem.isReposted,
                                        isPlaylist
                                    )
                                )
                            }
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_repost),
                            contentDescription = null,
                            tint = if (playlistItem.isReposted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatCount(playlistItem.repostCount),
                            style = typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Favorite button
                    Row(
                        modifier = Modifier
                            .clickable {
                                onAction(
                                    FeedAction.FavoritePressed(
                                        playlistItem.id,
                                        playlistItem.title,
                                        playlistItem._data.user,
                                        playlistItem.songIconList,
                                        !playlistItem.isFavorite
                                    )
                                )
                            }
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (playlistItem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (playlistItem.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatCount(playlistItem.favoriteCount),
                            style = typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Comment button
                    Row(
                        modifier = Modifier
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatCount(playlistItem.commentCount),
                            style = typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Plays count text
                    Text(
                        text = "${formatCount(playlistItem.playCount)} ${if (playlistItem.playCount == 1) "Play" else "Plays"}",
                        style = typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }
    }
}

private fun formatDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return ""
    return try {
        val instant = Instant.parse(dateStr)
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val month = localDate.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        "$month ${localDate.day}, ${localDate.year}"
    } catch (e: Exception) {
        try {
            val parts = dateStr.substringBefore("T").split("-")
            if (parts.size == 3) {
                val year = parts[0]
                val monthInt = parts[1].toIntOrNull() ?: 1
                val day = parts[2].toIntOrNull() ?: 1
                val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                val monthStr = months.getOrNull(monthInt) ?: "Jan"
                "$monthStr $day, $year"
            } else {
                dateStr
            }
        } catch (ex: Exception) {
            dateStr
        }
    }
}

@Composable
fun FilterPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = MaterialTheme.colorScheme.primary
    val textColor = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val backgroundColor = if (isSelected) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    val borderColor = if (isSelected) accentColor else Color.Transparent

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
            ),
            color = textColor
        )
    }
}
