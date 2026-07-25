package com.rld.justlisten.ui.bottombars.sheets.components

import com.rld.justlisten.datalayer.models.Comment
import com.rld.justlisten.viewmodel.comments.containsComment
import com.rld.justlisten.viewmodel.comments.indexOfCommentOrParent
import com.rld.justlisten.viewmodel.comments.preservingOptimisticCommentsFrom
import com.rld.justlisten.viewmodel.comments.restoringComment
import com.rld.justlisten.viewmodel.comments.withCommentReaction
import com.rld.justlisten.viewmodel.comments.withOptimisticComment
import com.rld.justlisten.viewmodel.comments.withoutComment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommentOptimisticUpdatesTest {
    @Test
    fun reactionUpdatesImmediatelyAndCanBeRolledBack() {
        val comment = comment(id = "comment", reactCount = 2)

        val reacted = listOf(comment).withCommentReaction("comment", isReacted = true)
        assertTrue(reacted.single().isCurrentUserReacted)
        assertEquals(3, reacted.single().reactCount)

        val rolledBack = reacted.withCommentReaction("comment", isReacted = false)
        assertFalse(rolledBack.single().isCurrentUserReacted)
        assertEquals(2, rolledBack.single().reactCount)
    }

    @Test
    fun reactionUpdateFindsNestedReply() {
        val reply = comment(id = "reply")
        val parent = comment(id = "parent").copy(replies = listOf(reply), replyCount = 1)

        val reacted = listOf(parent).withCommentReaction("reply", isReacted = true)

        assertTrue(reacted.single().replies.orEmpty().single().isCurrentUserReacted)
        assertEquals(1, reacted.single().replies.orEmpty().single().reactCount)
    }

    @Test
    fun optimisticReplyIsAddedAndRemovedWithItsParentCount() {
        val parent = comment(id = "parent")
        val reply = comment(id = "pending", parentId = parent.id)

        val added = listOf(parent).withOptimisticComment(reply)
        assertEquals(1, added.single().replyCount)
        assertEquals(listOf(reply), added.single().replies)

        val removed = added.withoutComment(reply.id)
        assertEquals(0, removed.single().replyCount)
        assertTrue(removed.single().replies.orEmpty().isEmpty())
    }

    @Test
    fun optimisticRootCommentCanBeRolledBack() {
        val existing = comment(id = "existing")
        val pending = comment(id = "pending")

        val added = listOf(existing).withOptimisticComment(pending)
        assertEquals(listOf(existing, pending), added)
        assertEquals(listOf(existing), added.withoutComment(pending.id))
    }

    @Test
    fun backgroundRefreshKeepsPendingUntilServerReturnsItsComment() {
        val pending = comment(id = "pending-comment-1")
        val unrelated = comment(id = "server-comment", createdAt = "2026-07-22T12:00:00Z")

        assertEquals(
            listOf(unrelated, pending),
            listOf(unrelated).preservingOptimisticCommentsFrom(listOf(pending)),
        )

        val indexed = pending.copy(id = "indexed-comment")
        assertEquals(
            listOf(indexed),
            listOf(indexed).preservingOptimisticCommentsFrom(listOf(pending)),
        )
    }

    @Test
    fun failedDeleteRestoresOnlyTheDeletedComment() {
        val first = comment(id = "first")
        val deleted = comment(id = "deleted")
        val concurrent = comment(id = "concurrent")

        assertEquals(
            listOf(first, deleted, concurrent),
            listOf(first, concurrent).restoringComment(deleted, index = 1),
        )
    }

    @Test
    fun sharedCommentLookupFindsRootAndNestedReply() {
        val reply = comment(id = "reply", parentId = "second")
        val comments = listOf(
            comment(id = "first"),
            comment(id = "second").copy(replies = listOf(reply), replyCount = 1),
        )

        assertTrue(comments.containsComment("first"))
        assertTrue(comments.containsComment("reply"))
        assertFalse(comments.containsComment("missing"))
        assertEquals(0, comments.indexOfCommentOrParent("first"))
        assertEquals(1, comments.indexOfCommentOrParent("reply"))
        assertEquals(-1, comments.indexOfCommentOrParent("missing"))
    }

    private fun comment(
        id: String,
        reactCount: Int = 0,
        parentId: String? = null,
        createdAt: String = "2026-07-23T12:00:00Z",
    ) = Comment(
        id = id,
        entityId = "track",
        userId = "user",
        message = "message",
        createdAt = createdAt,
        reactCount = reactCount,
        parentCommentId = parentId,
    )
}
