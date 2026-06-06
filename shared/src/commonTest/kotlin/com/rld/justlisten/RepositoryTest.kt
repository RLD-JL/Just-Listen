package com.rld.justlisten

import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.webservices.ApiClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class RepositoryTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testRepositoryInitialization() = runTest {
        val fakeSecureStorage = object : com.rld.justlisten.util.SecureStorage {
            override fun saveToken(key: String, value: String) {}
            override fun getToken(key: String): String? = null
            override fun clear() {}
        }
        val repository = Repository(null, ApiClient(secureStorage = fakeSecureStorage), false)
        assertNotNull(repository.webservices)
    }
}
