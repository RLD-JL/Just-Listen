package com.rld.justlisten.media

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlaybackQueueTransitionTest {
    @Test
    fun acceptsOnlyTheExpectedItemThatActuallyBecameCurrent() {
        assertTrue(
            canAcceptPreloadedTransition(
                expectedSongId = "next",
                preloadedSongId = "next",
                hasPreloadedItem = true,
                currentItemMatchesPreload = true,
                preloadedItemFailed = false,
            )
        )
    }

    @Test
    fun rejectsAStalePreloadReferenceWhenTheRealQueueIsEmpty() {
        assertFalse(
            canAcceptPreloadedTransition(
                expectedSongId = "next",
                preloadedSongId = "next",
                hasPreloadedItem = true,
                currentItemMatchesPreload = false,
                preloadedItemFailed = false,
            )
        )
    }

    @Test
    fun rejectsFailedOrWrongPreloadedItems() {
        assertFalse(
            canAcceptPreloadedTransition(
                expectedSongId = "next",
                preloadedSongId = "other",
                hasPreloadedItem = true,
                currentItemMatchesPreload = true,
                preloadedItemFailed = false,
            )
        )
        assertFalse(
            canAcceptPreloadedTransition(
                expectedSongId = "next",
                preloadedSongId = "next",
                hasPreloadedItem = true,
                currentItemMatchesPreload = true,
                preloadedItemFailed = true,
            )
        )
    }
}
