package com.rld.justlisten.media

import kotlin.test.Test
import kotlin.test.assertEquals

class PlaybackQueueNavigatorTest {

    @Test
    fun testGetNextIndexSequential() {
        val playlistSize = 5
        val shuffledIndices = emptyList<Int>()

        // 1. Normal transition (0 -> 1)
        assertEquals(
            1,
            PlaybackQueueNavigator.getNextIndex(0, playlistSize, shuffledIndices, false, RepeatMode.NONE)
        )

        // 2. End of playlist with RepeatMode.NONE should return -1
        assertEquals(
            -1,
            PlaybackQueueNavigator.getNextIndex(4, playlistSize, shuffledIndices, false, RepeatMode.NONE)
        )

        // 3. End of playlist with RepeatMode.ALL should return 0
        assertEquals(
            0,
            PlaybackQueueNavigator.getNextIndex(4, playlistSize, shuffledIndices, false, RepeatMode.ALL)
        )
    }

    @Test
    fun testGetNextIndexShuffle() {
        val playlistSize = 5
        val shuffledIndices = listOf(3, 1, 4, 0, 2)

        // 1. Transition in shuffle mode (index 3 -> index 1)
        assertEquals(
            1,
            PlaybackQueueNavigator.getNextIndex(3, playlistSize, shuffledIndices, true, RepeatMode.NONE)
        )

        // 2. End of shuffle list with RepeatMode.NONE should return -1
        assertEquals(
            -1,
            PlaybackQueueNavigator.getNextIndex(2, playlistSize, shuffledIndices, true, RepeatMode.NONE)
        )

        // 3. End of shuffle list with RepeatMode.ALL should wrap to first shuffled element (3)
        assertEquals(
            3,
            PlaybackQueueNavigator.getNextIndex(2, playlistSize, shuffledIndices, true, RepeatMode.ALL)
        )
    }

    @Test
    fun testGetPreviousIndexSequential() {
        val playlistSize = 5
        val shuffledIndices = emptyList<Int>()

        // 1. Normal transition (1 -> 0)
        assertEquals(
            0,
            PlaybackQueueNavigator.getPreviousIndex(1, playlistSize, shuffledIndices, false, RepeatMode.NONE)
        )

        // 2. Start of playlist with RepeatMode.NONE should return -1
        assertEquals(
            -1,
            PlaybackQueueNavigator.getPreviousIndex(0, playlistSize, shuffledIndices, false, RepeatMode.NONE)
        )

        // 3. Start of playlist with RepeatMode.ALL should wrap to last element (4)
        assertEquals(
            4,
            PlaybackQueueNavigator.getPreviousIndex(0, playlistSize, shuffledIndices, false, RepeatMode.ALL)
        )
    }

    @Test
    fun testGetPreviousIndexShuffle() {
        val playlistSize = 5
        val shuffledIndices = listOf(3, 1, 4, 0, 2)

        // 1. Transition in shuffle mode (index 1 -> index 3)
        assertEquals(
            3,
            PlaybackQueueNavigator.getPreviousIndex(1, playlistSize, shuffledIndices, true, RepeatMode.NONE)
        )

        // 2. Start of shuffle list with RepeatMode.NONE should return -1
        assertEquals(
            -1,
            PlaybackQueueNavigator.getPreviousIndex(3, playlistSize, shuffledIndices, true, RepeatMode.NONE)
        )

        // 3. Start of shuffle list with RepeatMode.ALL should wrap to last shuffled element (2)
        assertEquals(
            2,
            PlaybackQueueNavigator.getPreviousIndex(3, playlistSize, shuffledIndices, true, RepeatMode.ALL)
        )
    }
}
