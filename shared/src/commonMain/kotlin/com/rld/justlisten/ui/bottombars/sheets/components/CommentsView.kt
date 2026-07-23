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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.Send
import com.rld.justlisten.viewmodel.settings.SettingsViewModel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.rld.justlisten.util.clipEntryOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Velocity
import coil3.compose.rememberAsyncImagePainter
import com.rld.justlisten.datalayer.models.Comment
import com.rld.justlisten.datalayer.models.CommentUserProfile
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.repositories.AuthRepository
import com.rld.justlisten.datalayer.repositories.SessionState
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.commentcalls.getTrackComments
import com.rld.justlisten.datalayer.webservices.apis.commentcalls.deleteComment
import com.rld.justlisten.datalayer.webservices.apis.commentcalls.postComment
import com.rld.justlisten.datalayer.webservices.apis.commentcalls.reactToComment
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserProfile
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.ui.extensions.noRippleClickable
import com.rld.justlisten.ui.theme.typography
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsView(
    trackId: String,
    onCloseBottomSheet: () -> Unit,
    onUserProfileClick: (userId: String, userName: String) -> Unit,
) {
    val apiClient = koinInject<ApiClient>()
    val authRepository = koinInject<AuthRepository>()
    val settingsViewModel = koinInject<SettingsViewModel>()
    val settingsState by settingsViewModel.settingsState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val sessionState by authRepository.sessionState.collectAsState()
    val isUserLoggedIn = sessionState is SessionState.Authenticated
    val currentUserId = (sessionState as? SessionState.Authenticated)?.userProfile?.userId

    var commentsList by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var usersMap by remember { mutableStateOf<Map<String, CommentUserProfile>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var commentText by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }
    var commentErrorMessage by remember { mutableStateOf<String?>(null) }
    var commentPendingDeletion by remember { mutableStateOf<Comment?>(null) }
    var isDeletingComment by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(CommentSortOption.Top) }
    var isCommentInputFocused by remember { mutableStateOf(false) }
    var reactingCommentIds by remember { mutableStateOf(emptySet<String>()) }

    val latestCommentInputFocused = rememberUpdatedState(isCommentInputFocused)
    val dismissKeyboardOnDownwardSwipe = remember(focusManager, keyboardController) {
        object : NestedScrollConnection {
            private var isDismissingKeyboardForGesture = false

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (
                    source == NestedScrollSource.UserInput &&
                    available.y > 0f &&
                    latestCommentInputFocused.value
                ) {
                    isDismissingKeyboardForGesture = true
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }

                return if (isDismissingKeyboardForGesture) available else Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (!isDismissingKeyboardForGesture) return Velocity.Zero
                isDismissingKeyboardForGesture = false
                return available
            }
        }
    }

    // Replying state representation
    var replyingToComment by remember { mutableStateOf<Comment?>(null) }

    val musicPlayer = LocalMusicPlayer.current
    val currentMedia = musicPlayer.playbackState.collectAsState().value.currentMedia

    // Fetch comments function
    val loadComments: () -> Unit = {
        coroutineScope.launch {
            isLoading = true
            try {
                val response = apiClient.getTrackComments(trackId, limit = 50, offset = 0)
                if (response != null) {
                    commentsList = response.data
                    val users = response.related?.users ?: emptyList()
                    val relatedUsers = users.associateBy { it.id }
                    usersMap = relatedUsers
                    isLoading = false

                    // Audius omits nested reply authors from related.users. Resolve only
                    // those missing profiles so replies show their real name and avatar.
                    val missingReplyAuthorIds = response.data
                        .flatMap { it.replies.orEmpty() }
                        .map { it.userId }
                        .distinct()
                        .filterNot(relatedUsers::containsKey)
                    val replyAuthors = missingReplyAuthorIds.map { userId ->
                        async {
                            try {
                                apiClient.getUserProfile(userId)?.let { profile ->
                                    CommentUserProfile(
                                        id = profile.id,
                                        name = profile.name,
                                        handle = profile.handle,
                                        profilePicture = profile.profilePicture?.let { images ->
                                            SongIconList(
                                                songImageURL150px = images.image150.orEmpty(),
                                                songImageURL480px = images.image480.orEmpty(),
                                                songImageURL1000px = images.image1000.orEmpty(),
                                            )
                                        },
                                        isVerified = profile.isVerified,
                                    )
                                }
                            } catch (error: CancellationException) {
                                throw error
                            } catch (error: Exception) {
                                Logger.w(error) { "Unable to load reply author $userId" }
                                null
                            }
                        }
                    }.awaitAll().filterNotNull()
                    if (replyAuthors.isNotEmpty()) {
                        usersMap = relatedUsers + replyAuthors.associateBy { it.id }
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Logger.e(error) { "Unable to load comments for track $trackId" }
            } finally {
                isLoading = false
            }
        }
    }

    val submitComment: () -> Unit = submitComment@{
        val text = commentText.trim()
        val userId = currentUserId
        if (text.isEmpty() || userId == null || isPosting) return@submitComment

        isPosting = true
        commentErrorMessage = null
        coroutineScope.launch {
            try {
                val response = apiClient.postComment(
                    userId = userId,
                    trackId = trackId,
                    message = text,
                    parentId = replyingToComment?.id
                )
                if (response != null && response.error == null) {
                    commentText = ""
                    replyingToComment = null
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    loadComments()
                } else {
                    commentErrorMessage = response?.error ?: "Comment could not be posted. Please try again."
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Logger.e(error) { "Unable to post comment for track $trackId" }
                commentErrorMessage = "Comment could not be posted. Please try again."
            } finally {
                isPosting = false
            }
        }
    }

    LaunchedEffect(trackId) {
        loadComments()
    }

    commentPendingDeletion?.let { comment ->
        AlertDialog(
            onDismissRequest = {
                if (!isDeletingComment) commentPendingDeletion = null
            },
            title = { Text("Delete comment?") },
            text = { Text("This comment will be permanently deleted from Audius.") },
            confirmButton = {
                TextButton(
                    enabled = !isDeletingComment,
                    onClick = {
                        val userId = currentUserId ?: return@TextButton
                        isDeletingComment = true
                        commentErrorMessage = null
                        coroutineScope.launch {
                            try {
                                val response = apiClient.deleteComment(userId, comment.id)
                                if (response != null && response.error == null) {
                                    commentsList = commentsList.filterNot { it.id == comment.id }
                                    commentPendingDeletion = null
                                    com.rld.justlisten.ui.utils.showToast("Comment deleted")
                                    loadComments()
                                } else {
                                    commentErrorMessage = response?.error
                                        ?: "Comment could not be deleted. Please try again."
                                }
                            } catch (error: CancellationException) {
                                throw error
                            } catch (error: Exception) {
                                Logger.e(error) { "Unable to delete comment ${comment.id}" }
                                commentErrorMessage = "Comment could not be deleted. Please try again."
                            } finally {
                                isDeletingComment = false
                            }
                        }
                    },
                ) {
                    if (isDeletingComment) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeletingComment,
                    onClick = { commentPendingDeletion = null },
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .noRippleClickable {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
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

        // Replying To Banner
        if (replyingToComment != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val replyingUserName = usersMap[replyingToComment?.userId]?.name ?: "User"
                Text(
                    text = "Replying to @$replyingUserName",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                IconButton(
                    onClick = { replyingToComment = null },
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel reply",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

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
                        contentDescription = "Open your profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                val profile = (sessionState as? SessionState.Authenticated)?.userProfile
                                val userId = profile?.userId
                                if (!userId.isNullOrBlank()) {
                                    onUserProfileClick(userId, profile.name)
                                }
                            }
                    )

                    // Text Field Container Row
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = {
                                commentText = it
                                commentErrorMessage = null
                            },
                            placeholder = { Text(if (replyingToComment != null) "Add a reply..." else "Add a comment...", fontSize = 13.sp) },
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
                                .onFocusChanged { isCommentInputFocused = it.isFocused },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = { submitComment() }
                            )
                        )

                        IconButton(
                            onClick = submitComment,
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

        commentErrorMessage?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 64.dp)
            )
        }

        // Comment ordering
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CommentSortOption.entries.forEach { filter ->
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
                        text = filter.label,
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
                val activeCommentsList = remember(
                    commentsList,
                    settingsState.blockedUsers,
                    selectedFilter,
                ) {
                    commentsList.filter { comment ->
                        settingsState.blockedUsers.none { it.userId == comment.userId }
                    }.sortedForDisplay(selectedFilter)
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(dismissKeyboardOnDownwardSwipe),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(activeCommentsList, key = Comment::id) { comment ->
                        val isCommentHidden = settingsState.hiddenComments.contains(comment.id)
                        if (isCommentHidden) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Comment hidden",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "Unhide",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        settingsViewModel.unhideComment(comment.id)
                                    }
                                )
                            }
                        } else {
                            val commenter = usersMap[comment.userId]
                            val commenterName = commenter?.name ?: "User"
                            val commenterAvatar = commenter?.profilePicture?.songImageURL150px ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb"

                            Column(modifier = Modifier.fillMaxWidth()) {
                            // "Liked by Artist" header (simulated for popular/reacted comments)
                            val hasLikes = comment.reactCount > 0 || comment.isArtistReacted
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
                                    contentDescription = "Open $commenterName profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            onUserProfileClick(comment.userId, commenterName)
                                        }
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
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.clickable {
                                                onUserProfileClick(comment.userId, commenterName)
                                            },
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
                                        val timestampSec = comment.trackTimestampS
                                        if (timestampSec != null && timestampSec > 0) {
                                            val m = timestampSec / 60
                                            val s = timestampSec % 60
                                            Text(
                                                text = "• $m:${s.toString().padStart(2, '0')}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else if (hasLikes) {
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
                                        // Like Heart Toggle
                                        val isLiked = comment.isCurrentUserReacted
                                        val isUpdatingReaction = comment.id in reactingCommentIds
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.clickable {
                                                if (isUserLoggedIn && currentUserId != null) {
                                                    if (comment.id in reactingCommentIds) return@clickable
                                                    reactingCommentIds = reactingCommentIds + comment.id
                                                    commentErrorMessage = null
                                                    coroutineScope.launch {
                                                        try {
                                                            val updatedIsLiked = !isLiked
                                                            val success = apiClient.reactToComment(
                                                                userId = currentUserId,
                                                                commentId = comment.id,
                                                                trackId = trackId,
                                                                react = updatedIsLiked
                                                            )
                                                            if (success != null && success.error == null) {
                                                                commentsList = commentsList.map { currentComment ->
                                                                    if (currentComment.id == comment.id) {
                                                                        currentComment.copy(
                                                                            isCurrentUserReacted = updatedIsLiked,
                                                                            reactCount = (
                                                                                currentComment.reactCount +
                                                                                    if (updatedIsLiked) 1 else -1
                                                                                ).coerceAtLeast(0),
                                                                        )
                                                                    } else {
                                                                        currentComment
                                                                    }
                                                                }
                                                            } else {
                                                                commentErrorMessage = success?.error
                                                                    ?: "Comment reaction could not be updated. Please try again."
                                                            }
                                                        } catch (error: CancellationException) {
                                                            throw error
                                                        } catch (error: Exception) {
                                                            Logger.e(error) {
                                                                "Unable to update reaction for comment ${comment.id}"
                                                            }
                                                            commentErrorMessage =
                                                                "Comment reaction could not be updated. Please try again."
                                                        } finally {
                                                            reactingCommentIds = reactingCommentIds - comment.id
                                                        }
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Like",
                                                tint = when {
                                                    isUpdatingReaction -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                                    isLiked -> Color(0xFFE91E63)
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                },
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (comment.reactCount > 0) comment.reactCount.toString() else "0",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }

                                        // Reply button triggers "replyingToComment" banner
                                        Text(
                                            text = "Reply",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.clickable {
                                                if (isUserLoggedIn) {
                                                    replyingToComment = comment
                                                }
                                            }
                                        )

                                        // Share specific comment via Ellipsis Menu
                                        var showMenu by remember { mutableStateOf(false) }
                                        Box {
                                            Icon(
                                                imageVector = Icons.Default.MoreHoriz,
                                                contentDescription = "More Options",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable { showMenu = true }
                                            )
                                            val clipboard = LocalClipboard.current
                                            DropdownMenu(
                                                expanded = showMenu,
                                                onDismissRequest = { showMenu = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Copy Link to Comment") },
                                                    onClick = {
                                                        showMenu = false
                                                        val url = "justlisten://comments/share?trackId=$trackId&commentId=${comment.id}"
                                                        coroutineScope.launch {
                                                            clipboard.setClipEntry(clipEntryOf(url))
                                                        }
                                                        com.rld.justlisten.ui.utils.showToast("Comment link copied!")
                                                    },
                                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                                                )
                                                if (comment.userId == currentUserId) {
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                text = "Delete Comment",
                                                                color = MaterialTheme.colorScheme.error,
                                                            )
                                                        },
                                                        onClick = {
                                                            showMenu = false
                                                            commentPendingDeletion = comment
                                                        },
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.error,
                                                            )
                                                        },
                                                    )
                                                }
                                                val isCommentHiddenMenu = settingsState.hiddenComments.contains(comment.id)
                                                DropdownMenuItem(
                                                    text = { Text(if (isCommentHiddenMenu) "Unhide Comment" else "Hide Comment") },
                                                    onClick = {
                                                        showMenu = false
                                                        if (isCommentHiddenMenu) {
                                                            settingsViewModel.unhideComment(comment.id)
                                                        } else {
                                                            settingsViewModel.hideComment(comment.id)
                                                        }
                                                    },
                                                    leadingIcon = { Icon(if (isCommentHiddenMenu) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null) }
                                                )
                                                val isUserBlocked = settingsState.blockedUsers.any { it.userId == comment.userId }
                                                if (!isUserBlocked) {
                                                    DropdownMenuItem(
                                                        text = { Text("Block User") },
                                                        onClick = {
                                                            showMenu = false
                                                            settingsViewModel.blockUser(comment.userId, commenterName)
                                                        },
                                                        leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Nested replies rendering
                                    val repliesList = comment.replies ?: emptyList()
                                    val commentReplyCount = comment.replyCount
                                    if (commentReplyCount > 0 || repliesList.isNotEmpty()) {
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
                                                text = if (showReplies) "Hide Replies" else "Show Replies (${if (repliesList.isNotEmpty()) repliesList.size else commentReplyCount})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }

                                        if (showReplies) {
                                            Spacer(modifier = Modifier.height(8.dp))

                                            if (repliesList.isNotEmpty()) {
                                                // Render actual replies from the model
                                                repliesList.forEach { reply ->
                                                    val replier = usersMap[reply.userId]
                                                    val replierName = replier?.name ?: "User"
                                                    val replierAvatar = replier?.profilePicture?.songImageURL150px.orEmpty()
                                                    val isReplierArtist = reply.userId == currentMedia?.artistId

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        if (replierAvatar.isNotBlank()) {
                                                            Image(
                                                                painter = rememberAsyncImagePainter(replierAvatar),
                                                                contentDescription = "Open $replierName profile",
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier
                                                                    .size(28.dp)
                                                                    .clip(CircleShape)
                                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                                    .clickable {
                                                                        onUserProfileClick(reply.userId, replierName)
                                                                    }
                                                            )
                                                        } else {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(28.dp)
                                                                    .clip(CircleShape)
                                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                                                                    .clickable {
                                                                        onUserProfileClick(reply.userId, replierName)
                                                                    },
                                                                contentAlignment = Alignment.Center,
                                                            ) {
                                                                Text(
                                                                    text = replierName.firstOrNull()?.uppercase() ?: "?",
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.primary,
                                                                )
                                                            }
                                                        }

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                Text(
                                                                    text = replierName,
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isReplierArtist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                                    modifier = Modifier.clickable {
                                                                        onUserProfileClick(reply.userId, replierName)
                                                                    },
                                                                )

                                                                if (replier?.isVerified == true) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Verified,
                                                                        contentDescription = "Verified",
                                                                        tint = MaterialTheme.colorScheme.primary,
                                                                        modifier = Modifier.size(12.dp)
                                                                    )
                                                                }

                                                                Text(
                                                                    text = "• ${formatTimeAgo(reply.createdAt)}",
                                                                    fontSize = 11.sp,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                                )

                                                                if (isReplierArtist) {
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
                                                            }

                                                            Spacer(modifier = Modifier.height(2.dp))

                                                            Text(
                                                                text = reply.message,
                                                                fontSize = 12.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                lineHeight = 16.sp
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
