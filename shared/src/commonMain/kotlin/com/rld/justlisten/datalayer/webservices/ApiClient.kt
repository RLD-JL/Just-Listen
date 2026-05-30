package com.rld.justlisten.datalayer.webservices

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

open class ApiClient(val discoveryNodeService: DiscoveryNodeService) {

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
    }


    suspend inline fun <reified T : Any> getResponse(endpoint: String): T? {
        val nodeService = discoveryNodeService
        val baseUrl = nodeService.getBestNode()
        val url = "${baseUrl}/v1$endpoint"
        return try {
            val response = client.get(url)
            val body = response.body<T>()
            if (response.status.isSuccess()) {
                val responseTime = response.responseTime.timestamp - response.requestTime.timestamp
                nodeService.updateNodePerformance(baseUrl, responseTime)
            }
            body
        } catch (e: Exception) {
            null
        }
    }
}

