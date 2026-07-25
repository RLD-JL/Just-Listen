package com.rld.justlisten.ui.bottombars.sheets.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.rld.justlisten.datalayer.models.Comment
import com.rld.justlisten.datalayer.models.CommentUserProfile
import com.rld.justlisten.viewmodel.comments.isOptimisticComment

internal data class CommentThreadState(
    val comment: Comment,
    val author: CommentUserProfile?,
    val replyAuthors: Map<String, CommentUserProfile>,
    val viewer: CommentViewerContext,
    val moderation: CommentModerationState,
    val targetCommentId: String?,
)

internal data class CommentViewerContext(
    val userId: String?,
    val artistId: String?,
    val isLoggedIn: Boolean,
)

internal data class CommentModerationState(
    val isHidden: Boolean,
    val isAuthorBlocked: Boolean,
)

internal data class CommentThreadActions(
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
internal fun CommentThreadItem(
    state: CommentThreadState,
    actions: CommentThreadActions,
) {
    val comment = state.comment
    if (state.moderation.isHidden) {
        HiddenCommentRow(
            onUnhide = { actions.onToggleHidden(comment.id, false) },
        )
        return
    }

    val commenterName = state.author?.name ?: "User"
    val commenterAvatar = state.author?.profilePicture?.songImageURL150px
        ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb"
    val isSharedComment = comment.id == state.targetCommentId
    val isLikedByArtist = comment.isArtistReacted

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSharedComment) {
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(10.dp)
                } else {
                    Modifier
                }
            ),
    ) {
        if (isLikedByArtist) {
            Text(
                text = "💜 Liked by Artist",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 48.dp, bottom = 4.dp),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CommentAvatar(
                imageUrl = commenterAvatar,
                name = commenterName,
                size = 36.dp,
                onClick = { actions.onUserProfileClick(comment.userId, commenterName) },
            )

            Column(modifier = Modifier.weight(1f)) {
                CommentAuthorLine(
                    name = commenterName,
                    isVerified = state.author?.isVerified == true,
                    isArtist = false,
                    createdAt = comment.createdAt,
                    isPending = comment.isOptimisticComment(),
                    compact = false,
                    trackTimestampS = comment.trackTimestampS,
                    onNameClick = {
                        actions.onUserProfileClick(comment.userId, commenterName)
                    },
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comment.message,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                )
                Spacer(modifier = Modifier.height(6.dp))

                CommentActions(
                    state = state,
                    actions = actions,
                    commenterName = commenterName,
                )

                CommentReplies(
                    state = state,
                    actions = actions,
                )
            }
        }
    }
}

@Composable
private fun HiddenCommentRow(onUnhide: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Comment hidden",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
        Text(
            text = "Unhide",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onUnhide),
        )
    }
}

@Composable
private fun CommentActions(
    state: CommentThreadState,
    actions: CommentThreadActions,
    commenterName: String,
) {
    val comment = state.comment
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.clickable {
                if (state.viewer.isLoggedIn && !comment.isOptimisticComment()) {
                    actions.onToggleReaction(comment)
                }
            },
        ) {
            Icon(
                imageVector = if (comment.isCurrentUserReacted) {
                    Icons.Default.Favorite
                } else {
                    Icons.Default.FavoriteBorder
                },
                contentDescription = "Like",
                tint = if (comment.isCurrentUserReacted) {
                    Color(0xFFE91E63)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = comment.reactCount.coerceAtLeast(0).toString(),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }

        Text(
            text = "Reply",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.clickable {
                if (state.viewer.isLoggedIn && !comment.isOptimisticComment()) {
                    actions.onReply(comment)
                }
            },
        )

        CommentOptionsMenu(
            state = state,
            actions = actions,
            commenterName = commenterName,
        )
    }
}

@Composable
private fun CommentOptionsMenu(
    state: CommentThreadState,
    actions: CommentThreadActions,
    commenterName: String,
) {
    val comment = state.comment
    var expanded by remember { mutableStateOf(false) }
    Box {
        Icon(
            imageVector = Icons.Default.MoreHoriz,
            contentDescription = "More Options",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp).clickable {
                if (!comment.isOptimisticComment()) expanded = true
            },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Share Comment") },
                onClick = { expanded = false; actions.onShare(comment.id) },
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
            )
            DropdownMenuItem(
                text = { Text("Copy Link to Comment") },
                onClick = { expanded = false; actions.onCopyLink(comment.id) },
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
            )
            if (comment.userId == state.viewer.userId) {
                DropdownMenuItem(
                    text = { Text("Delete Comment", color = MaterialTheme.colorScheme.error) },
                    onClick = { expanded = false; actions.onDelete(comment) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                )
            }
            DropdownMenuItem(
                text = {
                    Text(if (state.moderation.isHidden) "Unhide Comment" else "Hide Comment")
                },
                onClick = {
                    expanded = false
                    actions.onToggleHidden(comment.id, !state.moderation.isHidden)
                },
                leadingIcon = {
                    Icon(
                        if (state.moderation.isHidden) {
                            Icons.Default.Visibility
                        } else {
                            Icons.Default.VisibilityOff
                        },
                        contentDescription = null,
                    )
                },
            )
            if (!state.moderation.isAuthorBlocked) {
                DropdownMenuItem(
                    text = { Text("Block User") },
                    onClick = {
                        expanded = false
                        actions.onBlockUser(comment.userId, commenterName)
                    },
                    leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) },
                )
            }
        }
    }
}

@Composable
private fun CommentReplies(
    state: CommentThreadState,
    actions: CommentThreadActions,
) {
    val parent = state.comment
    val replies = parent.replies.orEmpty()
    if (parent.replyCount <= 0 && replies.isEmpty()) return

    var expanded by remember(parent.id, state.targetCommentId) {
        mutableStateOf(replies.any { it.id == state.targetCommentId })
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { expanded = !expanded }.padding(vertical = 4.dp),
    ) {
        Icon(
            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (expanded) {
                "Hide Replies"
            } else {
                "Show Replies (${replies.size.takeIf { it > 0 } ?: parent.replyCount})"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }

    if (!expanded || replies.isEmpty()) return
    Spacer(modifier = Modifier.height(8.dp))
    replies.forEach { reply ->
        CommentReplyItem(
            reply = reply,
            replier = state.replyAuthors[reply.userId],
            isArtist = reply.userId == state.viewer.artistId,
            isShared = reply.id == state.targetCommentId,
            onUserProfileClick = actions.onUserProfileClick,
            onShare = { actions.onShare(reply.id) },
        )
    }
}

@Composable
private fun CommentReplyItem(
    reply: Comment,
    replier: CommentUserProfile?,
    isArtist: Boolean,
    isShared: Boolean,
    onUserProfileClick: (String, String) -> Unit,
    onShare: () -> Unit,
) {
    val name = replier?.name ?: "User"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isShared) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                else Color.Transparent
            )
            .padding(horizontal = if (isShared) 8.dp else 0.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CommentAvatar(
            imageUrl = replier?.profilePicture?.songImageURL150px.orEmpty(),
            name = name,
            size = 28.dp,
            onClick = { onUserProfileClick(reply.userId, name) },
        )
        Column(modifier = Modifier.weight(1f)) {
            CommentAuthorLine(
                name = name,
                isVerified = replier?.isVerified == true,
                isArtist = isArtist,
                createdAt = reply.createdAt,
                isPending = reply.isOptimisticComment(),
                compact = true,
                onNameClick = { onUserProfileClick(reply.userId, name) },
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = reply.message,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
            )
            Text(
                text = "Share",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp).clickable(onClick = onShare),
            )
        }
    }
}

@Composable
private fun CommentAuthorLine(
    name: String,
    isVerified: Boolean,
    isArtist: Boolean,
    createdAt: String,
    isPending: Boolean,
    compact: Boolean,
    onNameClick: () -> Unit,
    trackTimestampS: Int? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = name,
            fontSize = if (compact) 12.sp else 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isArtist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.clickable(onClick = onNameClick),
        )
        if (isVerified) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "Verified",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (compact) 12.dp else 13.dp),
            )
        }
        Text(
            text = if (isPending) "• Sending…" else "• ${formatCommentTime(createdAt)}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
        val timestampSeconds = trackTimestampS?.takeIf { it > 0 }
        if (timestampSeconds != null) {
            Text(
                text = "• ${timestampSeconds / 60}:${(timestampSeconds % 60).toString().padStart(2, '0')}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (isArtist) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "★ Artist",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun CommentAvatar(
    imageUrl: String,
    name: String,
    size: Dp,
    onClick: () -> Unit,
) {
    if (imageUrl.isNotBlank()) {
        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = "Open $name profile",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick),
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun formatCommentTime(dateString: String): String = runCatching {
    dateString.take(10)
}.getOrDefault("recent")
