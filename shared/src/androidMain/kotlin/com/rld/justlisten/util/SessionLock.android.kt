package com.rld.justlisten.util

actual class SessionLock actual constructor() {
    private val monitor = Any()
    actual fun <T> withLock(block: () -> T): T = synchronized(monitor, block)
}
