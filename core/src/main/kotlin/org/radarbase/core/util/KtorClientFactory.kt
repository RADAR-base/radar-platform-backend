package org.radarbase.core.util

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import java.time.Duration

@Singleton
class KtorClientFactory {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }

    fun createClient(
        baseUrl: String,
        timeoutSeconds: Long,
        maxRetriesCount: Int,
    ): HttpClient =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(json)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = Duration.ofSeconds(timeoutSeconds).toMillis()
                connectTimeoutMillis = Duration.ofSeconds(timeoutSeconds).toMillis()
            }
            install(HttpRequestRetry) {
                maxRetries = maxRetriesCount
                retryOnExceptionIf { _, cause ->
                    cause is HttpRequestTimeoutException ||
                        cause is ConnectTimeoutException
                }
            }
            defaultRequest {
                url(baseUrl)
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
        }
}
