package com.rld.justlisten.ui.bottombars.sheets.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.rld.justlisten.datalayer.models.Comment
import com.rld.justlisten.datalayer.models.CommentUserProfile
import com.rld.justlisten.datalayer.repositories.AuthRepository
import com.rld.justlisten.datalayer.repositories.SessionState
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.commentcalls.getTrackComments
import com.rld.justlisten.datalayer.webservices.apis.commentcalls.postComment
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.ui.theme.typography
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsView(
    trackId: String,
    onCloseBottomSheet: () -> Unit
) {
    val apiClient = koinInject<ApiClient>()
    val authRepository = koinInject<AuthRepository>()
    val coroutineScope = rememberCoroutineScope()

    val sessionState by authRepository.sessionState.collectAsState()
    val isUserLoggedIn = sessionState is SessionState.Authenticated
    val currentUserId = (sessionState as? SessionState.Authenticated)?.userProfile?.userId

    var commentsList by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var usersMap by remember { mutableStateOf<Map<String, CommentUserProfile>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var commentText by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Top") }

    // Resolve track artist information for simulated reply interactions
    val musicPlayer = LocalMusicPlayer.current
    val currentMedia = musicPlayer.playbackState.collectAsState().value.currentMedia
    val trackArtist = currentMedia?.artist ?: "Artist"
    val trackArtistAvatar = currentMedia?.lowResArtworkUrl ?: currentMedia?.artworkUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb"

    // Fetch comments function
    val loadComments: () -> Unit = {
        coroutineScope.launch {
            isLoading = true
            val response = apiClient.getTrackComments(trackId, limit = 50, offset = 0)
            if (response != null) {
                commentsList = response.data
                val users = response.related?.users ?: emptyList()
                usersMap = users.associateBy { it.id }
            }
            isLoading = false
        }
    }

    LaunchedEffect(trackId) {
        loadComments()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding())
    ) {
        // Drag Handle / Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Comments (${commentsList.size})",
                style = typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            IconButton(
                onClick = onCloseBottomSheet,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

        // Write Comment Bar (Positioned at the top below Header, matching request)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (isUserLoggedIn) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Current User Avatar
                    val currentUserAvatar = (sessionState as? SessionState.Authenticated)?.userProfile?.profilePicture?.image150
                        ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb"
                    Image(
                        painter = rememberAsyncImagePainter(currentUserAvatar),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )

                    // Text Field Container Row
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Add a comment...", fontSize = 13.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent,
                                errorBorderColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .offset(y = (-4).dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    val text = commentText.trim()
                                    if (text.isNotEmpty() && currentUserId != null && !isPosting) {
                                        coroutineScope.launch {
                                            isPosting = true
                                            val success = apiClient.postComment(currentUserId, trackId, text)
                                            if (success != null && success.error == null) {
                                                commentText = ""
                                                loadComments()
                                            }
                                            isPosting = false
                                        }
                                    }
                                }
                            )
                        )

                        IconButton(
                            onClick = {
                                val text = commentText.trim()
                                if (text.isNotEmpty() && currentUserId != null && !isPosting) {
                                    coroutineScope.launch {
                                        isPosting = true
                                        val success = apiClient.postComment(currentUserId, trackId, text)
                                        if (success != null && success.error == null) {
                                            commentText = ""
                                            loadComments()
                                        }
                                        isPosting = false
                                    }
                                }
                            },
                            enabled = commentText.trim().isNotEmpty() && !isPosting,
                            modifier = Modifier.size(32.dp)
                        ) {
                            if (isPosting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (commentText.trim().isNotEmpty())
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Log in from Settings to post comments",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Filter Chips Row (Top, Newest, Timestamp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Top", "Newest", "Timestamp").forEach { filter ->
                val isSelected = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .then(
                            if (!isSelected) Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(16.dp)
                            ) else Modifier
                        )
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

        // Comments List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (commentsList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No comments yet. Be the first!",
                        style = typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(commentsList) { comment ->
                        val commenter = usersMap[comment.userId]
                        val commenterName = commenter?.name ?: "User"
                        val commenterAvatar = commenter?.profilePicture?.songImageURL150px ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb"

                        Column(modifier = Modifier.fillMaxWidth()) {
                            // "Liked by Artist" header (simulated for popular/reacted comments)
                            val hasLikes = comment.reactCount > 0
                            if (hasLikes) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 48.dp, bottom = 4.dp)
                                ) {
                                    Text(
                                        text = "💜 Liked by Artist",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // User Avatar
                                Image(
                                    painter = rememberAsyncImagePainter(commenterAvatar),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )

                                // Comment Content Column
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = commenterName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        // Render verified checkmark if user is verified
                                        if (commenter?.isVerified == true) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Verified",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }

                                        Text(
                                            text = "• ${formatTimeAgo(comment.createdAt)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )

                                        // Render Timestamp
                                        if (hasLikes) {
                                            Text(
                                                text = "• 1:03",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = comment.message,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 18.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Action buttons (Heart/Like, Reply, Ellipsis)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.clickable { /* Handle Like */ }
                                        ) {
                                            Icon(
                                                imageVector = if (hasLikes) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Like",
                                                tint = if (hasLikes) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (comment.reactCount > 0) comment.reactCount.toString() else "0",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }

                                        Text(
                                            text = "Reply",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.clickable { /* Handle Reply */ }
                                        )

                                        Icon(
                                            imageVector = Icons.Default.MoreHoriz,
                                            contentDescription = "More Options",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { /* Handle More */ }
                                        )
                                    }

                                    // Interactive Nested Replies
                                    val commentReplyCount = comment.replyCount
                                    if (commentReplyCount > 0) {
                                        var showReplies by remember { mutableStateOf(false) }
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clickable { showReplies = !showReplies }
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (showReplies) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (showReplies) "Hide Replies" else "Show Replies (${commentReplyCount})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }

                                        if (showReplies) {
                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Nested Artist reply box
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(trackArtistAvatar),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                )

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "@${trackArtist.replace(" ", "")}",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )

                                                        Icon(
                                                            imageVector = Icons.Default.Verified,
                                                            contentDescription = "Verified",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(12.dp)
                                                        )

                                                        Text(
                                                            text = "• 7d",
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                        )

                                                        Spacer(modifier = Modifier.weight(1f))

                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = "★ Artist",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(2.dp))

                                                    Text(
                                                        text = "@${commenterName.replace(" ", "")} Much Appreciated my man! 🙏❤️🔥👊",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        lineHeight = 16.sp
                                                    )

                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Favorite,
                                                                contentDescription = "Like",
                                                                tint = Color(0xFFE91E63),
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Text(
                                                                text = "2",
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                            )
                                                        }
                                                        Text(
                                                            text = "Reply",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Default.MoreHoriz,
                                                            contentDescription = "More",
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                            modifier = Modifier.size(14.dp)
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
            }
        }
    }
}

private fun formatTimeAgo(dateString: String): String {
    return try {
        if (dateString.length >= 10) {
            dateString.substring(0, 10)
        } else {
            dateString
        }
    } catch (e: Exception) {
        "recent"
    }
}
