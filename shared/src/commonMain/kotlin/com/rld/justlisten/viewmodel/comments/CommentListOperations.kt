package com.rld.justlisten.viewmodel.comments

import com.rld.justlisten.datalayer.models.Comment

internal fun List<Comment>.containsComment(commentId: String): Boolean = any { comment ->
    comment.id == commentId || comment.replies.orEmpty().any { it.id == commentId }
}

internal fun List<Comment>.indexOfCommentOrParent(commentId: String): Int = indexOfFirst { comment ->
    comment.id == commentId || comment.replies.orEmpty().any { it.id == commentId }
}

internal fun List<Comment>.withCommentReaction(
    commentId: String,
    isReacted: Boolean,
): List<Comment> = map { comment ->
    if (comment.id == commentId) {
        comment.withReactionState(isReacted)
    } else {
        val replies = comment.replies.orEmpty()
        val updatedReplies = replies.map { reply ->
            if (reply.id == commentId) reply.withReactionState(isReacted) else reply
        }
        if (updatedReplies == replies) comment else comment.copy(replies = updatedReplies)
    }
}

internal fun List<Comment>.findComment(commentId: String): Comment? {
    forEach { comment ->
        if (comment.id == commentId) return comment
        comment.replies.orEmpty().firstOrNull { it.id == commentId }?.let { return it }
    }
    return null
}

private fun Comment.withReactionState(isReacted: Boolean): Comment {
    if (isCurrentUserReacted == isReacted) return this
    return copy(
        isCurrentUserReacted = isReacted,
        reactCount = (reactCount + if (isReacted) 1 else -1).coerceAtLeast(0),
    )
}

internal fun List<Comment>.withOptimisticComment(comment: Comment): List<Comment> {
    val parentId = comment.parentCommentId ?: return this + comment
    return map { parent ->
        if (parent.id != parentId) {
            parent
        } else {
            parent.copy(
                replyCount = parent.replyCount + 1,
                replies = parent.replies.orEmpty() + comment,
            )
        }
    }
}

internal fun List<Comment>.withoutComment(commentId: String): List<Comment> = mapNotNull { comment ->
    if (comment.id == commentId) {
        null
    } else {
        val replies = comment.replies.orEmpty()
        val updatedReplies = replies.filterNot { it.id == commentId }
        if (updatedReplies.size == replies.size) {
            comment
        } else {
            comment.copy(
                replyCount = (comment.replyCount - 1).coerceAtLeast(0),
                replies = updatedReplies,
            )
        }
    }
}

internal fun List<Comment>.restoringComment(comment: Comment, index: Int): List<Comment> {
    if (any { it.id == comment.id }) return this
    val insertionIndex = index.coerceIn(0, size)
    return toMutableList().apply { add(insertionIndex, comment) }
}

internal fun Comment.isOptimisticComment(): Boolean = id.startsWith("pending-comment-")

internal fun List<Comment>.preservingOptimisticCommentsFrom(
    currentComments: List<Comment>,
): List<Comment> {
    val pendingRoots = currentComments.filter(Comment::isOptimisticComment)
    val pendingRepliesByParent = currentComments.associate { parent ->
        parent.id to parent.replies.orEmpty().filter(Comment::isOptimisticComment)
    }

    val rootsWithPendingReplies = map { fetchedParent ->
        val missingReplies = pendingRepliesByParent[fetchedParent.id].orEmpty().filter { pending ->
            fetchedParent.replies.orEmpty().none { fetched -> fetched.matchesPending(pending) }
        }
        if (missingReplies.isEmpty()) {
            fetchedParent
        } else {
            fetchedParent.copy(
                replyCount = fetchedParent.replyCount + missingReplies.size,
                replies = fetchedParent.replies.orEmpty() + missingReplies,
            )
        }
    }

    val missingRoots = pendingRoots.filter { pending ->
        rootsWithPendingReplies.none { fetched -> fetched.matchesPending(pending) }
    }
    return rootsWithPendingReplies + missingRoots
}

private fun Comment.matchesPending(pending: Comment): Boolean =
    userId == pending.userId &&
        message == pending.message &&
        parentCommentId == pending.parentCommentId &&
        runCatching {
            kotlin.time.Instant.parse(createdAt).epochSeconds >=
                kotlin.time.Instant.parse(pending.createdAt).epochSeconds - 300
        }.getOrDefault(false)
