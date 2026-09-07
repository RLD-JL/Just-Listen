package com.rld.justlisten.util

import platform.Foundation.NSRecursiveLock

actual class SessionLock actual constructor() {
    private val lock = NSRecursiveLock()
    actual fun <T> withLock(block: () -> T): T {
        lock.lock()
        return try { block() } finally { lock.unlock() }
    }
}
