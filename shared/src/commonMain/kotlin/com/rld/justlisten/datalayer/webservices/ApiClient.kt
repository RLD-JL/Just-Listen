package com.rld.justlisten.datalayer.webservices

import com.rld.justlisten.datalayer.utils.Constants.BASEURL
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import kotlinx.serialization.json.Json

class ApiClient {

    val baseUrl = "${BASEURL}/v1"

    val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = true
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }

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

