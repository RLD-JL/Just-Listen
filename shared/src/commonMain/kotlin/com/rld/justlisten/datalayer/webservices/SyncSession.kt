package com.rld.justlisten.datalayer.webservices

import com.rld.justlisten.util.SecureStorage
import com.rld.justlisten.util.authSessionLock
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException

class SyncSessionChanged : CancellationException("Synchronization session changed")

/** Identity survives token refresh, but never a logout/new login. */
class SyncSession(val userId: String, private val generation: String?) :
    AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<SyncSession> {
        fun capture(storage: SecureStorage, userId: String): SyncSession = authSessionLock.withLock {
            if (userId.isBlank() || storage.getToken("user_id") != userId ||
                storage.getToken("access_token").isNullOrBlank()) throw SyncSessionChanged()
            SyncSession(userId, storage.getToken("auth_session_id"))
        }
    }

    fun requireActive(storage: SecureStorage) {
        if (storage.getToken("user_id") != userId ||
            storage.getToken("auth_session_id") != generation ||
            storage.getToken("access_token").isNullOrBlank()) throw SyncSessionChanged()
    }
}
