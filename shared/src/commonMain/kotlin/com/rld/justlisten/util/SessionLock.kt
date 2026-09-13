package com.rld.justlisten.util

/** Short, non-suspending critical sections for credentials and sync commits. */
expect class SessionLock() {
    fun <T> withLock(block: () -> T): T
}

val authSessionLock = SessionLock()
