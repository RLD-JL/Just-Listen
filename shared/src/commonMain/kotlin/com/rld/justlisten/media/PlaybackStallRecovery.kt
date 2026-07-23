package com.rld.justlisten.media

internal fun shouldAttemptPlaybackStallRecovery(status: PlaybackStatus): Boolean =
    status == PlaybackStatus.PLAYING || status == PlaybackStatus.BUFFERING
