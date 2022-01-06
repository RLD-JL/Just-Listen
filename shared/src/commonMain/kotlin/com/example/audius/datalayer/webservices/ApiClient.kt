package com.example.audius.datalayer.webservices

import com.example.audius.datalayer.utils.Constants.BASEURL
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.*
import kotlinx.serialization.json.Json

class ApiClient {

    val baseUrl = "${BASEURL}/v1"

    val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json {
                useAlternativeNames = false // currently needed as a workaround for this bug:
                // https://github.com/Kotlin/kotlinx.serialization/issues/1450#issuecomment-841214332
                // it should get fixed in kotlinx-serialization-json:1.2.2
                ignoreUnknownKeys = true
            })
        }
        /* Ktor specific logging: reenable if needed to debug requests
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        */
    }


    suspend inline fun <reified T : Any> getResponse(endpoint: String): T? {
        val url = baseUrl + endpoint
        try {
            // please notice, Ktor Client is switching to a background thread under the hood
            // so the http call doesn't happen on the main thread, even if the coroutine has been launched on Dispatchers.Main
            return client.get<T>(url)
        } catch (e: Exception) {
            println("Something went wrong" + e.cause + " message= " + e.message)
        }
        return null
    }
}

