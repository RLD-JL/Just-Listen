package com.rld.justlisten.datalayer.repositories

interface SyncRetryScheduler {
    fun scheduleRetry(delayMs: Long)
    fun cancelRetry()
}

class NoOpSyncRetryScheduler : SyncRetryScheduler {
    override fun scheduleRetry(delayMs: Long) = Unit
    override fun cancelRetry() = Unit
}
