package com.rld.justlisten.ui.bottombars.sheets.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.util.clipEntryOf
import com.rld.justlisten.util.commentsShareUrl
import com.rld.justlisten.util.rememberShareLauncher
import com.rld.justlisten.viewmodel.comments.CommentsEffect
import com.rld.justlisten.viewmodel.comments.CommentsViewModel
import com.rld.justlisten.viewmodel.settings.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private const val DEFAULT_AVATAR =
    "https://images.unsplash.com/photo-1534528741775-53994a69daeb"

@Composable
fun CommentsView(
    trackId: String,
    targetCommentId: String? = null,
    onCloseBottomSheet: () -> Unit,
    onUserProfileClick: (userId: String, userName: String) -> Unit,
) {
    val viewModel = koinViewModel<CommentsViewModel>()
    val state by viewModel.state.collectAsState()
    val settingsViewModel = koinInject<SettingsViewModel>()
    val settingsState by settingsViewModel.settingsState.collectAsState()
    val currentArtistId = LocalMusicPlayer.current.playbackState
        .collectAsState().value.currentMedia?.artistId
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val shareLauncher = rememberShareLauncher()
    var selectedSort by remember(trackId) { mutableStateOf(CommentSortOption.Top) }

    LaunchedEffect(trackId, targetCommentId) {
        viewModel.load(trackId, targetCommentId)
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CommentsEffect.DismissKeyboard -> {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }

                CommentsEffect.CommentDeleted -> {
                    com.rld.justlisten.ui.utils.showToast("Comment deleted")
                }
            }
        }
    }

    state.pendingDeletion?.let {
        DeleteCommentDialog(
            isDeleting = state.isDeleting,
            onDismiss = { viewModel.requestDelete(null) },
            onConfirm = viewModel::confirmDelete,
        )
    }

    val viewer = state.viewer
    CommentsContent(
        state = CommentsContentState(
            comments = state.comments,
            users = state.users,
            currentUserId = viewer?.id,
            currentUserName = viewer?.name ?: "User",
            currentUserAvatar = viewer?.avatarUrl?.takeIf(String::isNotBlank) ?: DEFAULT_AVATAR,
            currentArtistId = currentArtistId,
            targetCommentId = state.targetCommentId,
            replyingTo = state.replyingTo,
            commentText = state.commentText,
            commentErrorMessage = state.commentErrorMessage,
            targetCommentMessage = state.targetCommentMessage,
            selectedSort = selectedSort,
            hiddenCommentIds = settingsState.hiddenComments.toSet(),
            blockedUserIds = settingsState.blockedUsers.mapTo(mutableSetOf()) { it.userId },
            isUserLoggedIn = viewer != null,
            isLoading = state.isLoading,
            isPosting = state.isPosting,
        ),
        actions = CommentsContentActions(
            onClose = onCloseBottomSheet,
            onCommentTextChange = viewModel::updateCommentText,
            onSubmit = viewModel::submitComment,
            onCancelReply = { viewModel.replyTo(null) },
            onSortSelected = { selectedSort = it },
            onUserProfileClick = onUserProfileClick,
            onToggleReaction = viewModel::toggleReaction,
            onReply = viewModel::replyTo,
            onShare = { commentId ->
                shareLauncher.share(
                    commentsShareUrl(trackId, commentId),
                    "Share comment",
                )
            },
            onCopyLink = { commentId ->
                coroutineScope.launch {
                    clipboard.setClipEntry(clipEntryOf(commentsShareUrl(trackId, commentId)))
                }
                com.rld.justlisten.ui.utils.showToast("Comment link copied!")
            },
            onDelete = viewModel::requestDelete,
            onToggleHidden = { commentId, shouldHide ->
                if (shouldHide) settingsViewModel.hideComment(commentId)
                else settingsViewModel.unhideComment(commentId)
            },
            onBlockUser = settingsViewModel::blockUser,
        ),
    )
}
@Composable
private fun DeleteCommentDialog(
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = { Text("Delete comment?") },
        text = { Text("This action cannot be undone.") },
        confirmButton = {
            TextButton(enabled = !isDeleting, onClick = onConfirm) {
                Text(if (isDeleting) "Deleting…" else "Delete")
            }
        },
        dismissButton = {
            TextButton(enabled = !isDeleting, onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
