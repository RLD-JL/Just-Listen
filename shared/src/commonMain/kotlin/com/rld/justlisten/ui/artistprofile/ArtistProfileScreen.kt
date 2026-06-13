package com.rld.justlisten.ui.artistprofile

import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Close
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
import com.rld.justlisten.ui.artistprofile.components.EditProfileDialog
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
    var showEditDialog by remember { mutableStateOf(false) }
    val showEditState = remember { mutableStateOf(false) }
    LaunchedEffect(showEditDialog) {
        showEditState.value = showEditDialog
    }
    LaunchedEffect(showEditState.value) {
        if (!showEditState.value) {
            showEditDialog = false
        }
    }

    if (showEditState.value) {
        val profile = artistProfileState.artistProfile
        if (profile != null) {
            EditProfileDialog(
                showDialog = showEditState,
                initialName = profile.name,
                initialBio = profile.bio,
                initialProfilePicUrl = profile.profilePicture?.image150 ?: profile.profilePicture?.image480 ?: profile.profilePicture?.image1000,
                initialCoverPhotoUrl = profile.coverPhoto?.image2000 ?: profile.coverPhoto?.image640,
                onSaveClicked = { name, bio, profilePicUrl, coverPhotoUrl ->
                    onAction(ArtistProfileAction.EditProfileSaved(name, bio, profilePicUrl, coverPhotoUrl))
                    showEditDialog = false
                }
            )
        }
    }

    if (artistProfileState.showConnectPrompt) {
        ConnectPromptDialog(
            onDismissRequest = { onAction(ArtistProfileAction.DismissConnectPrompt) },
            onConnectClick = { onAction(ArtistProfileAction.ConnectAudiusPressed) }
        )
    }

    if (artistProfileState.showSocialSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

        ModalBottomSheet(
            onDismissRequest = { onAction(ArtistProfileAction.DismissSocialSheet) },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = artistProfileState.socialSheetTitle,
                        style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { onAction(ArtistProfileAction.DismissSocialSheet) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (artistProfileState.isSocialLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (artistProfileState.socialUsersList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No users found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(artistProfileState.socialUsersList) { user ->
                            var isFollowingState by remember(user.id) { mutableStateOf(user.doesCurrentUserFollow) }
                            var isFollowInProgress by remember { mutableStateOf(false) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .clickable {
                                        onAction(ArtistProfileAction.ArtistClicked(user.id, user.name))
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val avatarUrl = user.profilePicture?.image150 ?: user.profilePicture?.image480 ?: ""
                                val avatarPainter = rememberAsyncImagePainter(
                                    avatarUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" }
                                )
                                Image(
                                    painter = avatarPainter,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "@${user.handle}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!user.bio.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = user.bio,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                val isSelf = artistProfileState.isCurrentUser && (user.id == artistProfileState.artistProfile?.id)
                                if (!isSelf) {
                                    if (isFollowingState) {
                                        Button(
                                            onClick = {
                                                if (!isFollowInProgress) {
                                                    isFollowInProgress = true
                                                    onAction(ArtistProfileAction.SocialFollowPressed(user.id))
                                                    isFollowingState = false
                                                    isFollowInProgress = false
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Following", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                if (!isFollowInProgress) {
                                                    isFollowInProgress = true
                                                    onAction(ArtistProfileAction.SocialFollowPressed(user.id))
                                                    isFollowingState = true
                                                    isFollowInProgress = false
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PersonAdd,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text("Follow", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                val listState = rememberLazyListState()
                val coroutineScope = rememberCoroutineScope()

                LazyColumn(
                    state = listState,
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
                            val bannerUrl = profile.coverPhoto?.image2000 ?: profile.coverPhoto?.image640 ?: ""
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

                            // Follow / Unfollow / Edit Profile Button
                            if (artistProfileState.isCurrentUser) {
                                Button(
                                    onClick = { showEditDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .height(38.dp)
                                        .offset(y = (-18).dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text(
                                        text = "Edit Profile",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
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
                                ProfileStatItem(
                                    label = "Followers",
                                    value = formatCount(profile.followerCount),
                                    modifier = Modifier.clickable {
                                        onAction(ArtistProfileAction.FollowersClicked)
                                    }
                                )
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)))
                                ProfileStatItem(
                                    label = "Following",
                                    value = formatCount(profile.followeeCount),
                                    modifier = Modifier.clickable {
                                        onAction(ArtistProfileAction.FollowingClicked)
                                    }
                                )
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)))
                                ProfileStatItem(
                                    label = "Tracks", 
                                    value = formatCount(profile.trackCount),
                                    modifier = Modifier.clickable {
                                        onAction(ArtistProfileAction.TabSelected(0))
                                        coroutineScope.launch {
                                            val targetIndex = if (!profile.bio.isNullOrBlank()) 4 else 3
                                            listState.animateScrollToItem(targetIndex)
                                        }
                                    }
                                )
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
fun ProfileStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
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
