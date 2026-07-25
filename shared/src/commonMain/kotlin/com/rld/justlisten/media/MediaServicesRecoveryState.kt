package com.rld.justlisten.media

internal data class MediaServicesRecoverySnapshot(
    val mediaId: String,
    val positionMs: Long,
    val wasPlaying: Boolean,
)

internal sealed class MediaServicesResumeDecision {
    data object NoRecovery : MediaServicesResumeDecision()
    data object AwaitingReset : MediaServicesResumeDecision()
    data object AlreadyResuming : MediaServicesResumeDecision()
    data class Start(
        val generation: Long,
        val snapshot: MediaServicesRecoverySnapshot,
    ) : MediaServicesResumeDecision()
}

/**
 * Keeps media-server recovery deterministic without depending on AVFoundation.
 * A generation token prevents a slow resume from installing audio objects that
 * belong to an older media-services reset.
 */
internal class MediaServicesRecoveryState {
    private sealed class Phase {
        data object Idle : Phase()
        data class Lost(
            val generation: Long,
            val snapshot: MediaServicesRecoverySnapshot,
            val resumeRequested: Boolean,
        ) : Phase()

        data class Pending(
            val generation: Long,
            val snapshot: MediaServicesRecoverySnapshot,
        ) : Phase()

        data class Resuming(
            val generation: Long,
            val snapshot: MediaServicesRecoverySnapshot,
        ) : Phase()
    }

    private var generation = 0L
    private var phase: Phase = Phase.Idle

    fun onLost(snapshot: MediaServicesRecoverySnapshot) {
        generation += 1L
        phase = Phase.Lost(generation, snapshot, resumeRequested = false)
    }

    /** Returns true when Play was pressed while media services were unavailable. */
    fun onReset(snapshot: MediaServicesRecoverySnapshot): Boolean {
        val lost = phase as? Phase.Lost
        if (lost != null) {
            phase = Phase.Pending(lost.generation, lost.snapshot)
            return lost.resumeRequested
        }

        generation += 1L
        phase = Phase.Pending(generation, snapshot)
        return false
    }

    fun beginResume(): MediaServicesResumeDecision = when (val current = phase) {
        Phase.Idle -> MediaServicesResumeDecision.NoRecovery
        is Phase.Lost -> {
            phase = current.copy(resumeRequested = true)
            MediaServicesResumeDecision.AwaitingReset
        }
        is Phase.Resuming -> MediaServicesResumeDecision.AlreadyResuming
        is Phase.Pending -> {
            phase = Phase.Resuming(current.generation, current.snapshot)
            MediaServicesResumeDecision.Start(current.generation, current.snapshot)
        }
    }

    fun isCurrent(decision: MediaServicesResumeDecision.Start): Boolean {
        val current = phase as? Phase.Resuming ?: return false
        return current.generation == decision.generation
    }

    fun resumeSucceeded(decision: MediaServicesResumeDecision.Start): Boolean {
        if (!isCurrent(decision)) return false
        phase = Phase.Idle
        return true
    }

    fun resumeFailed(decision: MediaServicesResumeDecision.Start): Boolean {
        if (!isCurrent(decision)) return false
        phase = Phase.Pending(decision.generation, decision.snapshot)
        return true
    }

    fun cancelResume(): Boolean {
        val lost = phase as? Phase.Lost
        if (lost != null && lost.resumeRequested) {
            phase = lost.copy(resumeRequested = false)
            return true
        }

        val current = phase as? Phase.Resuming ?: return false
        phase = Phase.Pending(current.generation, current.snapshot)
        return true
    }

    fun clear() {
        generation += 1L
        phase = Phase.Idle
    }
}
