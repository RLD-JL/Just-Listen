package com.rld.justlisten.media

internal fun canAcceptPreloadedTransition(
    expectedSongId: String,
    preloadedSongId: String?,
    hasPreloadedItem: Boolean,
    currentItemMatchesPreload: Boolean,
    preloadedItemFailed: Boolean,
): Boolean =
    preloadedSongId == expectedSongId &&
        hasPreloadedItem &&
        currentItemMatchesPreload &&
        !preloadedItemFailed
