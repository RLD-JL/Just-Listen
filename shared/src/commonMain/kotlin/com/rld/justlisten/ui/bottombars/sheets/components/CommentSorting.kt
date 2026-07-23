package com.rld.justlisten.ui.bottombars.sheets.components

import com.rld.justlisten.datalayer.models.Comment

internal enum class CommentSortOption(val label: String) {
    Top("Top"),
    Newest("Newest"),
    Oldest("Oldest"),
}

internal fun List<Comment>.sortedForDisplay(sortOption: CommentSortOption): List<Comment> =
    when (sortOption) {
        CommentSortOption.Top -> sortedWith(
            compareByDescending<Comment> { it.reactCount + it.replyCount }
                .thenByDescending { it.reactCount }
                .thenByDescending { it.createdAt },
        )

        CommentSortOption.Newest -> sortedByDescending(Comment::createdAt)
        CommentSortOption.Oldest -> sortedBy(Comment::createdAt)
    }
