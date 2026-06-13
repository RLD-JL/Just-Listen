package com.rld.justlisten.ui.playlistscreen.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import com.rld.justlisten.datalayer.repositories.SessionState
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
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.ui.actions.PlaylistScreenAction
import com.rld.justlisten.ui.theme.typography
import com.rld.justlisten.viewmodel.screens.playlist.*
import com.rld.justlisten.viewmodel.screens.search.TrackItem
import com.rld.justlisten.ui.utils.getGreetingText

import com.rld.justlisten.ui.components.MusicLoadingSpinner
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.media.PlaybackStatus
import androidx.compose.ui.graphics.Color
import com.rld.justlisten.ui.components.AnimatedShimmer
import androidx.compose.runtime.collectAsState
import coil3.compose.AsyncImagePainter

@Composable
fun ScrollableContent(
    lasItemReached: (Int, PlayListEnum) -> Unit,
    scrollState: ScrollState,
    playlistState: PlaylistState,
    onAction: (PlaylistScreenAction) -> Unit
) {
    val musicPlayer = LocalMusicPlayer.current
    val playbackState by musicPlayer.playbackState.collectAsState()
    val currentPlayingSongId = playbackState.currentMedia?.id
    val currentlyPlayingPlaylistId = musicPlayer.currentlyPlayingPlaylistId
    val isPlaying = playbackState.status == PlaybackStatus.PLAYING

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .verticalScroll(scrollState)
    ) {
        // 1. Dynamic Greeting Header Row at the top
        val greetingText = getGreetingText()
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (playlistState.sessionState is SessionState.Authenticated) {
                    val avatarUrl = playlistState.sessionState.userProfile.profilePicture?.image150
                    val context = LocalPlatformContext.current
                    val painter = rememberAsyncImagePainter(
                        model = remember(avatarUrl, context) {
                            ImageRequest.Builder(context)
                                .data(avatarUrl)
                                .build()
                        }
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUrl != null) {
                            Image(
                                painter = painter,
                                contentDescription = "User Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User Profile",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                
                Column {
                    Text(
                        text = greetingText,
                        style = typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (playlistState.sessionState is SessionState.Authenticated) {
                        Text(
                            text = playlistState.sessionState.userProfile.name,
                            style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (playlistState.sessionState is SessionState.Authenticated) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onAction(PlaylistScreenAction.NotificationsClicked) }
                    )
                }
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onAction(PlaylistScreenAction.SearchClicked) }
                )
            }
        }

        // 2. Scrollable Genre Filter Pills (Dynamic themed)
        val genres = getTrackCategory()
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(genres, key = { it.name }) { genre ->
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
                imageVector = Icons.AutoMirrored.Filled.List,
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
                    items(chunkedTracks, key = { chunk -> chunk.firstOrNull()?.id ?: "" }) { pair ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isPlaying0 = isPlaying && pair[0].id == currentPlayingSongId
                            TrackCardItem(
                                track = pair[0],
                                onClick = {
                                    onAction(PlaylistScreenAction.SongPressed(pair[0].id, pair[0].title, pair[0].user, pair[0].songIconList))
                                },
                                onArtistClicked = { id, name ->
                                    onAction(PlaylistScreenAction.ArtistClicked(id, name))
                                },
                                isPlaying = isPlaying0
                            )
                            if (pair.size > 1) {
                                val isPlaying1 = isPlaying && pair[1].id == currentPlayingSongId
                                TrackCardItem(
                                    track = pair[1],
                                    onClick = {
                                        onAction(PlaylistScreenAction.SongPressed(pair[1].id, pair[1].title, pair[1].user, pair[1].songIconList))
                                    },
                                    onArtistClicked = { id, name ->
                                        onAction(PlaylistScreenAction.ArtistClicked(id, name))
                                    },
                                    isPlaying = isPlaying1
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
            },
            onArtistClicked = { id, name ->
                onAction(PlaylistScreenAction.ArtistClicked(id, name))
            },
            currentPlayingSongId = currentPlayingSongId,
            currentlyPlayingPlaylistId = currentlyPlayingPlaylistId,
            isPlaying = isPlaying
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TrackCardItem(
    track: TrackItem,
    onClick: () -> Unit,
    onArtistClicked: (String, String) -> Unit,
    isPlaying: Boolean = false
) {
    val context = LocalPlatformContext.current
    val painter = rememberAsyncImagePainter(
        model = remember(track.songIconList.songImageURL480px, context) {
            ImageRequest.Builder(context)
                .data(track.songIconList.songImageURL480px)
                .build()
        }
    )
    val state by painter.state.collectAsState()
    
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
                if (state is AsyncImagePainter.State.Loading) {
                    AnimatedShimmer(56.dp, 56.dp)
                }
                if (isPlaying) {
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
                val artistId = track._data.user.id
                Text(
                    text = track.user,
                    style = typography.bodySmall.copy(
                        color = if (artistId.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.then(
                        if (artistId.isNotBlank()) {
                            Modifier.clickable { onArtistClicked(artistId, track.user) }
                        } else Modifier
                    )
                )
            }
        }
    }
}
