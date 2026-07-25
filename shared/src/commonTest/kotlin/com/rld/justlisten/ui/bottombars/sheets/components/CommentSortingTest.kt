package com.rld.justlisten.ui.bottombars.sheets.components

import com.rld.justlisten.datalayer.models.Comment
import kotlin.test.Test
import kotlin.test.assertEquals

class CommentSortingTest {
    private val oldest = comment(
        id = "oldest",
        createdAt = "2026-01-01T00:00:00Z",
        reactCount = 2,
    )
    private val newest = comment(
        id = "newest",
        createdAt = "2026-03-01T00:00:00Z",
    )
    private val top = comment(
        id = "top",
        createdAt = "2026-02-01T00:00:00Z",
        reactCount = 3,
        replyCount = 2,
    )
    private val comments = listOf(oldest, newest, top)

    @Test
    fun sortsTopByEngagement() {
        assertEquals(
            listOf("top", "oldest", "newest"),
            comments.sortedForDisplay(CommentSortOption.Top).map(Comment::id),
        )
    }

    @Test
    fun sortsNewestByCreationTimeDescending() {
        assertEquals(
            listOf("newest", "top", "oldest"),
            comments.sortedForDisplay(CommentSortOption.Newest).map(Comment::id),
        )
    }

    @Test
    fun sortsOldestByCreationTimeAscending() {
        assertEquals(
            listOf("oldest", "top", "newest"),
            comments.sortedForDisplay(CommentSortOption.Oldest).map(Comment::id),
        )
    }

    @Test
    fun reactionCountUpdateKeepsExistingDisplayOrder() {
        val initialOrder = comments
            .sortedForDisplay(CommentSortOption.Top)
            .map(Comment::id)
        val reactedComments = comments.map { comment ->
            if (comment.id == "newest") {
                comment.copy(reactCount = 10, isCurrentUserReacted = true)
            } else {
                comment
            }
        }

        val displayedComments = reactedComments.orderedByIds(initialOrder)

        assertEquals(initialOrder, displayedComments.map(Comment::id))
        assertEquals(10, displayedComments.single { it.id == "newest" }.reactCount)
    }

    private fun comment(
        id: String,
        createdAt: String,
        reactCount: Int = 0,
        replyCount: Int = 0,
    ) = Comment(
        id = id,
        entityId = "track",
        userId = "user",
        message = id,
        createdAt = createdAt,
        reactCount = reactCount,
        replyCount = replyCount,
    )
}
