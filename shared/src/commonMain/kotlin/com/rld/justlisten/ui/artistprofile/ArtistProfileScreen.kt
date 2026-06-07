package com.rld.justlisten.ui.artistprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.rld.justlisten.ui.actions.ArtistProfileAction
import com.rld.justlisten.ui.artistprofile.components.ConnectPromptDialog
import com.rld.justlisten.ui.seeallscreen.SeeAllListItem
import com.rld.justlisten.ui.seeallscreen.formatCount
import com.rld.justlisten.ui.theme.typography
import com.rld.justlisten.viewmodel.screens.artistprofile.ArtistProfileState
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.media.MusicPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistProfileScreen(
    artistProfileState: ArtistProfileState,
    musicPlayer: MusicPlayer,
    libraryRepository: LibraryRepository,
    onAction: (ArtistProfileAction) -> Unit
) {
    if (artistProfileState.showConnectPrompt) {
        ConnectPromptDialog(
            onDismissRequest = { onAction(ArtistProfileAction.DismissConnectPrompt) },
            onConnectClick = { onAction(ArtistProfileAction.ConnectAudiusPressed) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = artistProfileState.artistProfile?.name ?: "Artist Profile",
                        style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(ArtistProfileAction.BackPressed) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (artistProfileState.isLoading) {
                com.rld.justlisten.ui.components.MusicLoadingScreen(showText = true)
            } else if (artistProfileState.artistProfile == null) {
                Text(
                    text = "Failed to load profile",
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val profile = artistProfileState.artistProfile
                val context = LocalPlatformContext.current

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // 1. Banner Image with Gradient overlay
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            val bannerUrl = profile.coverPhoto?.image1000 ?: profile.coverPhoto?.image480 ?: ""
                            if (bannerUrl.isNotBlank()) {
                                val bannerPainter = rememberAsyncImagePainter(
                                    model = remember(bannerUrl, context) {
                                        ImageRequest.Builder(context).data(bannerUrl).build()
                                    }
                                )
                                Image(
                                    painter = bannerPainter,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            // Bottom gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                                        )
                                    )
                            )
                        }
                    }

                    // 2. Avatar & Name/Handle Card (overlap)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .offset(y = (-40).dp)
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                val avatarUrl = profile.profilePicture?.image150 ?: ""
                                if (avatarUrl.isNotBlank()) {
                                    val avatarPainter = rememberAsyncImagePainter(
                                        model = remember(avatarUrl, context) {
                                            ImageRequest.Builder(context).data(avatarUrl).build()
                                        }
                                    )
                                    Image(
                                        painter = avatarPainter,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            // Name, verified badge, and handle
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.offset(y = (-30).dp)
                            ) {
                                Text(
                                    text = profile.name,
                                    style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (profile.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = "@${profile.handle}",
                                style = typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.offset(y = (-28).dp)
                            )

                            // Follow / Unfollow Button
                            Button(
                                onClick = { onAction(ArtistProfileAction.FollowPressed) },
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(38.dp)
                                    .offset(y = (-18).dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = if (profile.doesCurrentUserFollow) {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            ) {
                                Text(
                                    text = if (profile.doesCurrentUserFollow) "Following" else "Follow",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // 3. User stats card (followers, following, tracks)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProfileStatItem(label = "Followers", value = formatCount(profile.followerCount))
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)))
                                ProfileStatItem(label = "Following", value = formatCount(profile.followeeCount))
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)))
                                ProfileStatItem(label = "Tracks", value = formatCount(profile.trackCount))
                            }
                        }
                    }

                    // 4. Bio section
                    if (!profile.bio.isNullOrBlank()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                            ) {
                                Text(
                                    text = "About",
                                    style = typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = profile.bio,
                                    style = typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    // 5. Tabs (Tracks / Playlists)
                    item {
                        val tabs = listOf("Tracks", "Playlists")
                        TabRow(
                            selectedTabIndex = artistProfileState.selectedTabIndex,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = artistProfileState.selectedTabIndex == index,
                                    onClick = {
                                        onAction(ArtistProfileAction.TabSelected(index))
                                    },
                                    text = {
                                        Text(
                                            text = title,
                                            fontWeight = if (artistProfileState.selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                    }
                                )
                            }
                        }
                        // Note: The KMM TabRow takes a select action, so we must wire tab selections inside ViewModel.
                        // Let's implement ViewModel.onTabSelected called when tabs are clicked.
                        // Since tabs are wired differently, let's use an inline tab layout or standard toggling.
                    }

                    // 6. Tracks/Playlists items
                    if (artistProfileState.selectedTabIndex == 0) {
                        if (artistProfileState.artistTracks.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No tracks uploaded", style = typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                }
                            }
                        } else {
                            items(artistProfileState.artistTracks) { item ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                    SeeAllListItem(
                                        item = item,
                                        onClick = { onAction(ArtistProfileAction.SongPressed(item.id)) },
                                        onArtistClicked = { _, _ -> }
                                    )
                                }
                            }
                        }
                    } else {
                        if (artistProfileState.artistPlaylists.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No playlists found", style = typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                }
                            }
                        } else {
                            items(artistProfileState.artistPlaylists) { playlist ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                    ArtistPlaylistRowItem(
                                        playlist = playlist,
                                        onClick = {
                                            onAction(
                                                ArtistProfileAction.PlaylistClicked(
                                                    playlistId = playlist.id,
                                                    playlistIcon = playlist.songImgList.songImageURL480px,
                                                    createdBy = playlist.user.username,
                                                    title = playlist.playlistTitle.ifBlank { playlist.title }
                                                )
                                            )
                                        }
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

@Composable
fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ArtistPlaylistRowItem(
    playlist: PlayListModel,
    onClick: () -> Unit
) {
    val context = LocalPlatformContext.current
    val painter = rememberAsyncImagePainter(
        model = remember(playlist.songImgList.songImageURL480px, context) {
            ImageRequest.Builder(context)
                .data(playlist.songImgList.songImageURL480px)
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
                    com.rld.justlisten.ui.components.AnimatedShimmer(64.dp, 64.dp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = playlist.playlistTitle.ifBlank { playlist.title },
                    style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "by ${playlist.user.username}",
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
