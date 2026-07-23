package com.rld.justlisten.media

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlaybackStallRecoveryTest {
    @Test
    fun retriesPlaybackWhilePlayerIsPlayingOrBuffering() {
        assertTrue(shouldAttemptPlaybackStallRecovery(PlaybackStatus.PLAYING))
        assertTrue(shouldAttemptPlaybackStallRecovery(PlaybackStatus.BUFFERING))
    }

    @Test
    fun doesNotOverrideIntentionalOrTerminalStates() {
        assertFalse(shouldAttemptPlaybackStallRecovery(PlaybackStatus.PAUSED))
        assertFalse(shouldAttemptPlaybackStallRecovery(PlaybackStatus.STOPPED))
        assertFalse(shouldAttemptPlaybackStallRecovery(PlaybackStatus.IDLE))
        assertFalse(shouldAttemptPlaybackStallRecovery(PlaybackStatus.ERROR))
    }
}
