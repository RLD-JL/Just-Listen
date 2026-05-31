package com.rld.justlisten.datalayer.webservices

import com.rld.justlisten.datalayer.utils.Constants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

open class ApiClient(private val apiKey: String = "") {

    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 3000
            socketTimeoutMillis = 4500
            requestTimeoutMillis = 10000
        }
        install(HttpRequestRetry) {
            maxRetries = 3
            retryIf { _, response -> !response.status.isSuccess() }
            delayMillis { retry -> retry * 1000L }
        }
        defaultRequest {
            if (apiKey.isNotBlank()) {
                header("X-API-KEY", apiKey)
            }
        }
    }

    suspend inline fun <reified T : Any> getResponse(endpoint: String): T? {
        val url = "${Constants.BASEURL}/v1$endpoint"
        return try {
            val response = client.get(url)
            if (response.status.isSuccess()) response.body<T>() else null
        } catch (e: Exception) {
            println("Error fetching $url: ${e.message}")
            null
        }
    }
}