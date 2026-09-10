package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.util.SecureStorage
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class ArtistAccountContextTest {
    @Test
    fun salesIncludesAccountContextAndRetriesWithRenewedToken() = runBlocking {
        val values = mutableMapOf("user_id" to "PWr0k", "access_token" to "expired",
            "refresh_token" to "refresh", "auth_session_id" to "session")
        val storage = object : SecureStorage {
            override fun getToken(key: String) = values[key]
            override fun saveToken(key: String, value: String) { values[key] = value }
            override fun clear() { values.clear() }
        }
        var refreshes = 0
        val attempts = mutableListOf<String?>()
        val api = ApiClient(secureStorage = storage, httpClientEngine = MockEngine { request ->
            if (request.url.encodedPath.endsWith("/oauth/token")) {
                refreshes++
                respond("""{"access_token":"renewed","refresh_token":"new-refresh"}""",
                    HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            } else {
                assertEquals("PWr0k", request.url.parameters["user_id"])
                assertEquals("20", request.url.parameters["limit"])
                assertEquals(1, request.headers.getAll(HttpHeaders.Authorization)?.size)
                val bearer = request.headers[HttpHeaders.Authorization]
                attempts += bearer
                respond("""{"data":[]}""",
                    if (bearer == "Bearer expired") HttpStatusCode.Unauthorized else HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, "application/json"))
            }
        })
        try {
            api.getResponse<com.rld.justlisten.datalayer.models.SalesAggregateResponse>(
                "/users/PWr0k/sales/aggregate?limit=20")
            assertEquals(1, refreshes)
            assertEquals<List<String?>>(listOf("Bearer expired", "Bearer renewed"), attempts)
        } finally { api.client.close() }
    }
}
