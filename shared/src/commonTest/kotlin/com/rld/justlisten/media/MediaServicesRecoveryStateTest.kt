package com.rld.justlisten.media

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MediaServicesRecoveryStateTest {
    private val snapshot = MediaServicesRecoverySnapshot(
        mediaId = "track-1",
        positionMs = 42_000L,
        wasPlaying = true,
    )

    @Test
    fun resetPreservesPlaybackSnapshotAndRequiresExplicitResume() {
        val state = MediaServicesRecoveryState()

        state.onReset(snapshot)

        val decision = assertIs<MediaServicesResumeDecision.Start>(state.beginResume())
        assertEquals(snapshot, decision.snapshot)
        assertTrue(state.isCurrent(decision))
    }

    @Test
    fun playBetweenMediaServicesLostAndResetIsDeferredThenResumed() {
        val state = MediaServicesRecoveryState()
        state.onLost(snapshot)

        assertSame(MediaServicesResumeDecision.AwaitingReset, state.beginResume())
        assertSame(MediaServicesResumeDecision.AwaitingReset, state.beginResume())
        assertTrue(state.onReset(snapshot.copy(positionMs = 99_000L)))

        val decision = assertIs<MediaServicesResumeDecision.Start>(state.beginResume())
        assertEquals(snapshot, decision.snapshot)
    }

    @Test
    fun pauseCancelsPlayDeferredDuringMediaServicesLoss() {
        val state = MediaServicesRecoveryState()
        state.onLost(snapshot)
        state.beginResume()

        assertTrue(state.cancelResume())
        assertFalse(state.onReset(snapshot))
        assertIs<MediaServicesResumeDecision.Start>(state.beginResume())
    }

    @Test
    fun repeatedPlayWhileResumeIsRunningDoesNotStartAnotherPipeline() {
        val state = MediaServicesRecoveryState()
        state.onReset(snapshot)

        state.beginResume()

        assertSame(MediaServicesResumeDecision.AlreadyResuming, state.beginResume())
    }

    @Test
    fun failedOrCancelledResumeCanBeRetried() {
        val state = MediaServicesRecoveryState()
        state.onReset(snapshot)
        val failed = assertIs<MediaServicesResumeDecision.Start>(state.beginResume())

        assertTrue(state.resumeFailed(failed))
        assertIs<MediaServicesResumeDecision.Start>(state.beginResume())

        state.cancelResume()
        assertIs<MediaServicesResumeDecision.Start>(state.beginResume())
    }

    @Test
    fun newerResetInvalidatesAnOlderAsynchronousResume() {
        val state = MediaServicesRecoveryState()
        state.onReset(snapshot)
        val stale = assertIs<MediaServicesResumeDecision.Start>(state.beginResume())
        val newerSnapshot = snapshot.copy(mediaId = "track-2", positionMs = 7_000L)

        state.onReset(newerSnapshot)

        assertFalse(state.isCurrent(stale))
        assertFalse(state.resumeSucceeded(stale))
        val current = assertIs<MediaServicesResumeDecision.Start>(state.beginResume())
        assertEquals(newerSnapshot, current.snapshot)
    }

    @Test
    fun successfulResumeOrClearReturnsToNormalPlayback() {
        val state = MediaServicesRecoveryState()
        state.onReset(snapshot)
        val decision = assertIs<MediaServicesResumeDecision.Start>(state.beginResume())

        assertTrue(state.resumeSucceeded(decision))
        assertSame(MediaServicesResumeDecision.NoRecovery, state.beginResume())

        state.onReset(snapshot)
        state.clear()
        assertSame(MediaServicesResumeDecision.NoRecovery, state.beginResume())
    }
}
