package com.rld.justlisten.viewmodel.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.rld.justlisten.datalayer.models.Comment
import com.rld.justlisten.datalayer.models.CommentUserProfile
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.repositories.AuthRepository
import com.rld.justlisten.datalayer.repositories.CommentsRepository
import com.rld.justlisten.datalayer.repositories.SessionState
import com.rld.justlisten.datalayer.webservices.apis.authcalls.MeResponse
import com.rld.justlisten.datalayer.webservices.apis.authcalls.UserProfileModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val COMMENT_PAGE_SIZE = 50
private const val MAX_SHARED_COMMENT_LOOKUP_COUNT = 500

data class CommentsViewer(
    val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String,
    val avatarUrl480: String,
    val avatarUrl1000: String,
    val isVerified: Boolean,
)

data class CommentsState(
    val trackId: String = "",
    val targetCommentId: String? = null,
    val comments: List<Comment> = emptyList(),
    val users: Map<String, CommentUserProfile> = emptyMap(),
    val viewer: CommentsViewer? = null,
    val replyingTo: Comment? = null,
    val pendingDeletion: Comment? = null,
    val commentText: String = "",
    val commentErrorMessage: String? = null,
    val targetCommentMessage: String? = null,
    val isLoading: Boolean = true,
    val isPosting: Boolean = false,
    val isDeleting: Boolean = false,
)

sealed interface CommentsEffect {
    data object DismissKeyboard : CommentsEffect
    data object CommentDeleted : CommentsEffect
}

class CommentsViewModel(
    private val commentsRepository: CommentsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CommentsState())
    val state: StateFlow<CommentsState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CommentsEffect>(extraBufferCapacity = 2)
    val effects: SharedFlow<CommentsEffect> = _effects.asSharedFlow()

    private var loadJob: Job? = null
    private var stateVersion = 0
    private val reactionRequests = mutableMapOf<Pair<Int, String>, ReactionRequest>()

    init {
        viewModelScope.launch {
            authRepository.sessionState.collect { session ->
                val profile = (session as? SessionState.Authenticated)?.userProfile
                _state.update { it.copy(viewer = profile?.toCommentsViewer()) }
            }
        }
    }

    fun load(trackId: String, targetCommentId: String?) {
        val current = _state.value
        if (
            current.trackId == trackId &&
            current.targetCommentId == targetCommentId &&
            current.comments.isNotEmpty()
        ) return

        loadJob?.cancel()
        stateVersion += 1
        reactionRequests.clear()
        val version = stateVersion
        _state.update {
            CommentsState(
                trackId = trackId,
                targetCommentId = targetCommentId,
                viewer = it.viewer,
            )
        }
        loadJob = viewModelScope.launch { fetchComments(showLoading = true, version = version) }
    }

    fun updateCommentText(text: String) {
        _state.update { it.copy(commentText = text, commentErrorMessage = null) }
    }

    fun replyTo(comment: Comment?) {
        _state.update { it.copy(replyingTo = comment) }
    }

    fun requestDelete(comment: Comment?) {
        if (!_state.value.isDeleting) _state.update { it.copy(pendingDeletion = comment) }
    }

    fun submitComment() {
        val current = _state.value
        val text = current.commentText.trim()
        val viewer = current.viewer
        if (text.isEmpty() || viewer == null || current.isPosting) return

        val parent = current.replyingTo
        val version = stateVersion
        val now = kotlin.time.Clock.System.now()
        val optimisticComment = Comment(
            id = "pending-comment-${now.epochSeconds}",
            entityId = current.trackId,
            userId = viewer.id,
            message = text,
            createdAt = now.toString(),
            parentCommentId = parent?.id,
        )
        _state.update {
            it.copy(
                comments = it.comments.withOptimisticComment(optimisticComment),
                users = it.users + (viewer.id to viewer.toCommentUserProfile()),
                commentText = "",
                replyingTo = null,
                commentErrorMessage = null,
                isPosting = true,
            )
        }
        _effects.tryEmit(CommentsEffect.DismissKeyboard)

        viewModelScope.launch {
            try {
                val response = commentsRepository.postComment(
                    userId = viewer.id,
                    trackId = current.trackId,
                    message = text,
                    parentId = parent?.id,
                )
                if (response != null && response.error == null && isCurrent(version)) {
                    fetchComments(showLoading = false, version = version)
                } else {
                    rollbackPost(
                        version = version,
                        optimisticComment = optimisticComment,
                        draft = text,
                        parent = parent,
                        errorMessage = response?.error
                            ?: "Comment could not be posted. Please try again.",
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Logger.e(error) { "Unable to post comment for track ${current.trackId}" }
                rollbackPost(
                    version = version,
                    optimisticComment = optimisticComment,
                    draft = text,
                    parent = parent,
                    errorMessage = "Comment could not be posted. Please try again.",
                )
            } finally {
                updateCurrent(version) { it.copy(isPosting = false) }
            }
        }
    }

    fun toggleReaction(comment: Comment) {
        val current = _state.value
        val viewer = current.viewer ?: return
        val version = stateVersion
        val reactionKey = version to comment.id
        val latestComment = current.comments.findComment(comment.id) ?: return
        if (latestComment.isOptimisticComment()) return

        val requestedState = !latestComment.isCurrentUserReacted
        _state.update {
            it.copy(
                comments = it.comments.withCommentReaction(comment.id, requestedState),
                commentErrorMessage = null,
            )
        }
        reactionRequests[reactionKey]?.let { activeRequest ->
            activeRequest.desiredState = requestedState
            return
        }

        val request = ReactionRequest(
            confirmedState = latestComment.isCurrentUserReacted,
            desiredState = requestedState,
        )
        reactionRequests[reactionKey] = request
        viewModelScope.launch {
            try {
                processReactionChanges(
                    version = version,
                    commentId = comment.id,
                    viewerId = viewer.id,
                    trackId = current.trackId,
                    request = request,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Logger.e(error) { "Unable to update reaction for comment ${comment.id}" }
                handleReactionFailure(version, comment.id, request, null)
            } finally {
                if (reactionRequests[reactionKey] === request) {
                    reactionRequests.remove(reactionKey)
                }
            }
        }
    }

    private suspend fun processReactionChanges(
        version: Int,
        commentId: String,
        viewerId: String,
        trackId: String,
        request: ReactionRequest,
    ) {
        while (isCurrent(version) && request.confirmedState != request.desiredState) {
            val requestedState = request.desiredState
            val response = commentsRepository.setCommentReaction(
                userId = viewerId,
                commentId = commentId,
                trackId = trackId,
                reacted = requestedState,
            )
            if (response == null || response.error != null) {
                handleReactionFailure(version, commentId, request, response?.error)
                return
            }
            request.confirmedState = requestedState
        }
    }

    private fun handleReactionFailure(
        version: Int,
        commentId: String,
        request: ReactionRequest,
        serverMessage: String?,
    ) {
        if (request.desiredState == request.confirmedState) return
        rollbackReaction(
            version = version,
            commentId = commentId,
            isReacted = request.confirmedState,
            errorMessage = serverMessage
                ?: "Comment reaction could not be updated. Please try again.",
        )
        request.desiredState = request.confirmedState
    }

    fun confirmDelete() {
        val current = _state.value
        val comment = current.pendingDeletion ?: return
        val viewer = current.viewer ?: return
        if (current.isDeleting) return
        val version = stateVersion

        val deletedIndex = current.comments.indexOfFirst { it.id == comment.id }
        _state.update {
            it.copy(
                comments = it.comments.withoutComment(comment.id),
                pendingDeletion = null,
                commentErrorMessage = null,
                isDeleting = true,
            )
        }
        viewModelScope.launch {
            try {
                val response = commentsRepository.deleteComment(viewer.id, comment.id)
                if (response != null && response.error == null && isCurrent(version)) {
                    _effects.emit(CommentsEffect.CommentDeleted)
                    fetchComments(showLoading = false, version = version)
                } else {
                    restoreDeletedComment(
                        version,
                        comment,
                        deletedIndex,
                        response?.error ?: "Comment could not be deleted. Please try again.",
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Logger.e(error) { "Unable to delete comment ${comment.id}" }
                restoreDeletedComment(
                    version,
                    comment,
                    deletedIndex,
                    "Comment could not be deleted. Please try again.",
                )
            } finally {
                updateCurrent(version) { it.copy(isDeleting = false) }
            }
        }
    }

    private suspend fun fetchComments(showLoading: Boolean, version: Int) {
        if (!isCurrent(version)) return
        val request = _state.value
        if (request.trackId.isBlank()) return
        if (showLoading) updateCurrent(version) { it.copy(isLoading = true) }
        try {
            val fetchedComments = mutableListOf<Comment>()
            val fetchedUsers = mutableMapOf<String, CommentUserProfile>()
            var offset = 0
            var receivedResponse = false

            while (fetchedComments.size < MAX_SHARED_COMMENT_LOOKUP_COUNT) {
                val response = commentsRepository.getTrackComments(
                    trackId = request.trackId,
                    limit = COMMENT_PAGE_SIZE,
                    offset = offset,
                ) ?: break
                receivedResponse = true
                response.data.forEach { comment ->
                    if (fetchedComments.none { it.id == comment.id }) fetchedComments += comment
                }
                response.related?.users.orEmpty().forEach { fetchedUsers[it.id] = it }

                val foundTarget = request.targetCommentId == null ||
                    fetchedComments.containsComment(request.targetCommentId)
                if (foundTarget || response.data.size < COMMENT_PAGE_SIZE) break
                offset += response.data.size
            }

            if (receivedResponse && isCurrent(version)) {
                updateCurrent(version) { latest ->
                    val comments = if (showLoading) {
                        fetchedComments
                    } else {
                        fetchedComments.preservingOptimisticCommentsFrom(latest.comments)
                    }
                    latest.copy(
                        comments = comments,
                        users = if (showLoading) fetchedUsers else latest.users + fetchedUsers,
                        targetCommentMessage = request.targetCommentId
                            ?.takeUnless(comments::containsComment)
                            ?.let { "The shared comment is unavailable or no longer exists." },
                        isLoading = false,
                    )
                }
                resolveMissingReplyAuthors(fetchedComments, fetchedUsers, version)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Logger.e(error) { "Unable to load comments for track ${request.trackId}" }
        } finally {
            updateCurrent(version) { it.copy(isLoading = false) }
        }
    }

    private suspend fun resolveMissingReplyAuthors(
        comments: List<Comment>,
        relatedUsers: Map<String, CommentUserProfile>,
        version: Int,
    ) {
        val missingIds = comments
            .flatMap { it.replies.orEmpty() }
            .map(Comment::userId)
            .distinct()
            .filterNot(relatedUsers::containsKey)
        val replyAuthors = coroutineScope {
            missingIds.map { userId ->
                async {
                    try {
                        commentsRepository.getUserProfile(userId)?.toCommentUserProfile()
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        Logger.w(error) { "Unable to load reply author $userId" }
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }
        if (replyAuthors.isNotEmpty()) {
            updateCurrent(version) {
                it.copy(users = it.users + replyAuthors.associateBy(CommentUserProfile::id))
            }
        }
    }

    private fun rollbackPost(
        version: Int,
        optimisticComment: Comment,
        draft: String,
        parent: Comment?,
        errorMessage: String,
    ) {
        updateCurrent(version) {
            it.copy(
                comments = it.comments.withoutComment(optimisticComment.id),
                commentText = it.commentText.ifBlank { draft },
                replyingTo = if (it.commentText.isBlank()) parent else it.replyingTo,
                commentErrorMessage = errorMessage,
            )
        }
    }

    private fun rollbackReaction(
        version: Int,
        commentId: String,
        isReacted: Boolean,
        errorMessage: String,
    ) {
        updateCurrent(version) {
            it.copy(
                comments = it.comments.withCommentReaction(commentId, isReacted),
                commentErrorMessage = errorMessage,
            )
        }
    }

    private fun restoreDeletedComment(
        version: Int,
        comment: Comment,
        index: Int,
        errorMessage: String,
    ) {
        updateCurrent(version) {
            it.copy(
                comments = it.comments.restoringComment(comment, index),
                commentErrorMessage = errorMessage,
            )
        }
    }

    private fun isCurrent(version: Int): Boolean = version == stateVersion

    private inline fun updateCurrent(
        version: Int,
        transform: (CommentsState) -> CommentsState,
    ) {
        if (!isCurrent(version)) return
        _state.update { current -> if (isCurrent(version)) transform(current) else current }
    }
}

private fun MeResponse.toCommentsViewer(): CommentsViewer? = userId?.let { id ->
    CommentsViewer(
        id = id,
        name = name,
        handle = handle,
        avatarUrl = profilePicture?.image150.orEmpty(),
        avatarUrl480 = profilePicture?.image480.orEmpty(),
        avatarUrl1000 = profilePicture?.image1000.orEmpty(),
        isVerified = verified,
    )
}

private fun CommentsViewer.toCommentUserProfile() = CommentUserProfile(
    id = id,
    name = name,
    handle = handle,
    profilePicture = SongIconList(
        songImageURL150px = avatarUrl,
        songImageURL480px = avatarUrl480,
        songImageURL1000px = avatarUrl1000,
    ),
    isVerified = isVerified,
)

private fun UserProfileModel.toCommentUserProfile() = CommentUserProfile(
    id = id,
    name = name,
    handle = handle,
    profilePicture = profilePicture?.let { images ->
        SongIconList(
            songImageURL150px = images.image150.orEmpty(),
            songImageURL480px = images.image480.orEmpty(),
            songImageURL1000px = images.image1000.orEmpty(),
        )
    },
    isVerified = isVerified,
)

private data class ReactionRequest(
    var confirmedState: Boolean,
    var desiredState: Boolean,
)
