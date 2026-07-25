package com.rld.justlisten.ui.bottombars.sheets.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.rld.justlisten.datalayer.models.Comment
import com.rld.justlisten.datalayer.models.CommentUserProfile
import com.rld.justlisten.ui.extensions.noRippleClickable
import com.rld.justlisten.ui.theme.typography
import com.rld.justlisten.viewmodel.comments.indexOfCommentOrParent

internal data class CommentsContentState(
    val comments: List<Comment>,
    val users: Map<String, CommentUserProfile>,
    val currentUserId: String?,
    val currentUserName: String,
    val currentUserAvatar: String,
    val currentArtistId: String?,
    val targetCommentId: String?,
    val replyingTo: Comment?,
    val commentText: String,
    val commentErrorMessage: String?,
    val targetCommentMessage: String?,
    val selectedSort: CommentSortOption,
    val hiddenCommentIds: Set<String>,
    val blockedUserIds: Set<String>,
    val isUserLoggedIn: Boolean,
    val isLoading: Boolean,
    val isPosting: Boolean,
)

internal data class CommentsContentActions(
    val onClose: () -> Unit,
    val onCommentTextChange: (String) -> Unit,
    val onSubmit: () -> Unit,
    val onCancelReply: () -> Unit,
    val onSortSelected: (CommentSortOption) -> Unit,
    val onUserProfileClick: (String, String) -> Unit,
    val onToggleReaction: (Comment) -> Unit,
    val onReply: (Comment) -> Unit,
    val onShare: (String) -> Unit,
    val onCopyLink: (String) -> Unit,
    val onDelete: (Comment) -> Unit,
    val onToggleHidden: (String, Boolean) -> Unit,
    val onBlockUser: (String, String) -> Unit,
)

@Composable
internal fun CommentsContent(
    state: CommentsContentState,
    actions: CommentsContentActions,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    var inputFocused by remember { mutableStateOf(false) }
    val latestInputFocused = rememberUpdatedState(inputFocused)
    var hasScrolledToTarget by remember(state.targetCommentId) { mutableStateOf(false) }
    val dismissKeyboardOnDownwardSwipe = remember(focusManager, keyboardController) {
        object : NestedScrollConnection {
            private var dismissing = false

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (
                    source == NestedScrollSource.UserInput &&
                    available.y > 0f &&
                    latestInputFocused.value
                ) {
                    dismissing = true
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
                return if (dismissing) available else Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (!dismissing) return Velocity.Zero
                dismissing = false
                return available
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                MaterialTheme.colorScheme.background,
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            )
            .noRippleClickable {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
            .padding(bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()),
    ) {
        CommentsHeader(state.comments.size, actions.onClose)
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

        state.replyingTo?.let { replyingTo ->
            ReplyingBanner(
                userName = state.users[replyingTo.userId]?.name ?: "User",
                onCancel = actions.onCancelReply,
            )
        }

        CommentInput(
            state = state,
            onTextChange = actions.onCommentTextChange,
            onSubmit = actions.onSubmit,
            onFocusChanged = { inputFocused = it },
            onUserProfileClick = actions.onUserProfileClick,
        )

        state.commentErrorMessage?.let { message ->
            StatusMessage(message, MaterialTheme.colorScheme.error, horizontalPadding = 64)
        }
        state.targetCommentMessage?.let { message ->
            StatusMessage(message, MaterialTheme.colorScheme.onSurfaceVariant, horizontalPadding = 16)
        }

        CommentSortSelector(state.selectedSort, actions.onSortSelected)
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

        val commentIds = state.comments.map(Comment::id)
        val orderedVisibleCommentIds = remember(
            commentIds,
            state.blockedUserIds,
            state.selectedSort,
        ) {
            state.comments
                .filterNot { it.userId in state.blockedUserIds }
                .sortedForDisplay(state.selectedSort)
                .map(Comment::id)
        }
        val visibleComments = remember(state.comments, orderedVisibleCommentIds) {
            state.comments.orderedByIds(orderedVisibleCommentIds)
        }
        LaunchedEffect(state.targetCommentId, visibleComments) {
            val targetId = state.targetCommentId ?: return@LaunchedEffect
            if (hasScrolledToTarget) return@LaunchedEffect
            val targetIndex = visibleComments.indexOfCommentOrParent(targetId)
            if (targetIndex >= 0) {
                listState.animateScrollToItem(targetIndex)
                hasScrolledToTarget = true
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.isLoading -> LoadingComments()
                state.comments.isEmpty() -> EmptyComments()
                else -> {
                    val viewer = CommentViewerContext(
                        userId = state.currentUserId,
                        artistId = state.currentArtistId,
                        isLoggedIn = state.isUserLoggedIn,
                    )
                    val threadActions = CommentThreadActions(
                        onUserProfileClick = actions.onUserProfileClick,
                        onToggleReaction = actions.onToggleReaction,
                        onReply = actions.onReply,
                        onShare = actions.onShare,
                        onCopyLink = actions.onCopyLink,
                        onDelete = actions.onDelete,
                        onToggleHidden = actions.onToggleHidden,
                        onBlockUser = actions.onBlockUser,
                    )
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(dismissKeyboardOnDownwardSwipe),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(visibleComments, key = Comment::id) { comment ->
                            CommentThreadItem(
                                state = CommentThreadState(
                                    comment = comment,
                                    author = state.users[comment.userId],
                                    replyAuthors = state.users,
                                    viewer = viewer,
                                    moderation = CommentModerationState(
                                        isHidden = comment.id in state.hiddenCommentIds,
                                        isAuthorBlocked = comment.userId in state.blockedUserIds,
                                    ),
                                    targetCommentId = state.targetCommentId,
                                ),
                                actions = threadActions,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentsHeader(commentCount: Int, onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = "Comments ($commentCount)",
            style = typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterEnd)) {
            Icon(Icons.Default.Close, contentDescription = "Close")
        }
    }
}

@Composable
private fun ReplyingBanner(userName: String, onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Replying to @$userName",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
        )
        IconButton(onClick = onCancel, modifier = Modifier.size(16.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
private fun CommentInput(
    state: CommentsContentState,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onUserProfileClick: (String, String) -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (!state.isUserLoggedIn) {
            Text(
                text = "Log in from Settings to post comments",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center).padding(vertical = 4.dp),
            )
            return@Box
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = rememberAsyncImagePainter(state.currentUserAvatar),
                contentDescription = "Open your profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        state.currentUserId?.let { onUserProfileClick(it, state.currentUserName) }
                    },
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = state.commentText,
                    onValueChange = onTextChange,
                    placeholder = {
                        Text(if (state.replyingTo != null) "Add a reply..." else "Add a comment...", fontSize = 13.sp)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier.weight(1f).onFocusChanged { onFocusChanged(it.isFocused) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Send,
                    ),
                    keyboardActions = KeyboardActions(onSend = { onSubmit() }),
                )
                IconButton(
                    onClick = onSubmit,
                    enabled = state.commentText.trim().isNotEmpty() && !state.isPosting,
                    modifier = Modifier.size(32.dp),
                ) {
                    if (state.isPosting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (state.commentText.trim().isNotEmpty()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentSortSelector(
    selected: CommentSortOption,
    onSelected: (CommentSortOption) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CommentSortOption.entries.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .then(
                        if (isSelected) Modifier else Modifier.border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            RoundedCornerShape(16.dp),
                        )
                    )
                    .clickable { onSelected(option) }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text(
                    text = option.label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun StatusMessage(message: String, color: Color, horizontalPadding: Int) {
    Text(
        text = message,
        color = color,
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = horizontalPadding.dp, vertical = 4.dp),
    )
}

@Composable
private fun LoadingComments() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EmptyComments() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No comments yet. Be the first!",
            style = typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
