package com.rld.justlisten.ui.playlistscreen.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.ui.actions.PlaylistScreenAction
import com.rld.justlisten.ui.theme.typography
import com.rld.justlisten.viewmodel.screens.playlist.*
import com.rld.justlisten.viewmodel.screens.search.TrackItem
import java.util.*

import com.rld.justlisten.ui.components.MusicLoadingSpinner
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.media.PlaybackStatus
import androidx.compose.ui.graphics.Color

@Composable
fun ScrollableContent(
    lasItemReached: (Int, PlayListEnum) -> Unit,
    scrollState: ScrollState,
    playlistState: PlaylistState,
    onAction: (PlaylistScreenAction) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .verticalScroll(scrollState)
    ) {
        // 1. Dynamic Greeting Header Row at the top
        val rightNow = Calendar.getInstance()
        val greetingText = when (rightNow.get(Calendar.HOUR_OF_DAY)) {
            in 0..5 -> "Chilling"
            in 5..11 -> "Good Morning"
            in 12..17 -> "Hey there"
            in 17..23 -> "Good Evening"
            else -> "Hello"
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = greetingText,
                style = typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onAction(PlaylistScreenAction.SearchClicked) }
            )
        }

        // 2. Scrollable Genre Filter Pills (Dynamic themed)
        val genres = getTrackCategory()
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(genres) { genre ->
                val isSelected = genre == playlistState.selectedCategory
                val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(containerColor)
                        .clickable {
                            onAction(PlaylistScreenAction.ChangeTracksCategory(genre, playlistState.selectedTimeRange))
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

        // 3. Subheader with Week/Month/All Time tabs and sorting icon
        val timeRanges = getTimeRange()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                timeRanges.fastForEachIndexed { index, item ->
                    val isSelected = item == playlistState.selectedTimeRange
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            onAction(PlaylistScreenAction.ChangeTracksCategory(playlistState.selectedCategory, item))
                        }
                    ) {
                        Text(
                            text = item.value,
                            style = typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(2.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "See All Tracks",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        onAction(
                            PlaylistScreenAction.SeeAllTracksClicked(
                                categoryName = "${playlistState.selectedCategory.value} Trending",
                                queryPlaylist = playlistState.selectedCategory.value,
                                selectedTimeRange = playlistState.selectedTimeRange
                            )
                        )
                    }
            )
        }

        // 4. Dynamic horizontal LazyRow tracks slider (Replaces the hardcoded 2x2 grid with a 2-row scrolling grid)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (playlistState.tracksLoading) {
                MusicLoadingSpinner(size = 32.dp)
            } else if (playlistState.tracksList.isEmpty()) {
                Text(
                    text = "No tracks found",
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val chunkedTracks = playlistState.tracksList.chunked(2)
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(chunkedTracks) { pair ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TrackCardItem(
                                track = pair[0],
                                onClick = {
                                    onAction(PlaylistScreenAction.SongPressed(pair[0].id, pair[0].title, pair[0].user, pair[0].songIconList))
                                }
                            )
                            if (pair.size > 1) {
                                TrackCardItem(
                                    track = pair[1],
                                    onClick = {
                                        onAction(PlaylistScreenAction.SongPressed(pair[1].id, pair[1].title, pair[1].user, pair[1].songIconList))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Horizontal collections: Trending Now, Essential Rock, etc.
        ListOfCollections(
            playlistState = playlistState,
            lasItemReached = lasItemReached,
            onPlaylistClicked = { id, icon, createdBy, title, _ -> 
                onAction(PlaylistScreenAction.PlaylistClicked(id, icon, createdBy, title)) 
            },
            onSeeAllClicked = { item, playListEnum, query ->
                onAction(PlaylistScreenAction.SeeAllClicked(item, playListEnum, query))
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TrackCardItem(
    track: TrackItem,
    onClick: () -> Unit
) {
    val musicPlayer = LocalMusicPlayer.current
    val playbackState by musicPlayer.playbackState.collectAsState()
    val isPlayingThisTrack = playbackState.status == PlaybackStatus.PLAYING &&
            playbackState.currentMedia?.id == track.id

    val context = LocalPlatformContext.current
    val painter = rememberAsyncImagePainter(
        model = remember(track.songIconList.songImageURL480px, context) {
            ImageRequest.Builder(context)
                .data(track.songIconList.songImageURL480px)
                .allowHardware(true)
                .build()
        }
    )
    
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (isPlayingThisTrack) {
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
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = track.title,
                    style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = track.user,
                    style = typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
