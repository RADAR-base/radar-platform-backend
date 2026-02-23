package org.radarbase.delegate.service

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.net.SocketTimeoutException
import java.time.Duration

@Singleton
class HttpClientService
    @Inject
    constructor() {
        private val logger = LoggerFactory.getLogger(HttpClientService::class.java)

        val client: HttpClient =
            HttpClient(CIO) {
                install(ContentNegotiation) {
                    json()
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = Duration.ofSeconds(30).toMillis()
                    connectTimeoutMillis = Duration.ofSeconds(30).toMillis()
                }
                install(HttpRequestRetry) {
                    maxRetries = 3
                    retryOnExceptionIf { _, cause ->
                        cause is HttpRequestTimeoutException ||
                            cause is SocketTimeoutException
                    }
                }
            }

        suspend fun <T> get(
            url: String,
            responseHandler: (String) -> T,
            authToken: String? = null,
        ): T {
            try {
                val response =
                    client.get(url) {
                        authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    }
                if (response.status.isSuccess()) {
                    return responseHandler(response.bodyAsText())
                } else {
                    logger.error("HTTP request failed with status ${response.status}: ${response.bodyAsText()}")
                    throw RuntimeException("HTTP request failed with status ${response.status}")
                }
            } catch (e: Exception) {
                logger.error("Failed to make HTTP request to $url", e)
                throw e
            }
        }

        suspend inline fun <reified T> getJson(
            url: String,
            authToken: String? = null,
        ): T {
            val response =
                client.get(url) {
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                }
            return Json {
                ignoreUnknownKeys = true
            }.decodeFromString<T>(response.bodyAsText())
        }

        /**
         * POST with JSON body; returns (statusCode, responseBody).
         */
        suspend fun postWithBody(
            url: String,
            body: String,
            authToken: String? = null,
        ): Pair<Int, String> {
            val response =
                client.post(url) {
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
            return response.status.value to response.bodyAsText()
        }

        /**
         * PUT with JSON body; returns (statusCode, responseBody).
         */
        suspend fun putWithBody(
            url: String,
            body: String,
            authToken: String? = null,
        ): Pair<Int, String> {
            val response =
                client.put(url) {
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
            return response.status.value to response.bodyAsText()
        }

        /**
         * DELETE request; returns (statusCode, responseBody).
         */
        suspend fun delete(
            url: String,
            authToken: String? = null,
        ): Pair<Int, String> {
            val response =
                client.delete(url) {
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                }
            return response.status.value to response.bodyAsText()
        }
    }
