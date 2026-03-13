/*
 * Copyright 2026 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.contract.utils

import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import org.radarbase.contract.exception.InvalidUpstreamResponseException
import org.radarbase.contract.exception.ProxyResponseException
import org.radarbase.contract.response.ProxyResponse
import org.radarbase.jersey.exception.HttpNotFoundException
import org.slf4j.LoggerFactory
import java.net.ConnectException

/**
 * Shared utilities for the contract layer.
 *
 * Provides JSON configuration, proxy-response creation, typed
 * deserialization with faithful error mapping, and resilient
 * request wrapping.
 */
object ContractUtils {
    @PublishedApi
    internal val logger = LoggerFactory.getLogger(ContractUtils::class.java)

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    /**
     * Reads a Ktor [HttpResponse] into a [ProxyResponse], capturing
     * status, Content-Type, Location header and body bytes.
     */
    suspend fun createProxyFromResponse(resp: HttpResponse): ProxyResponse =
        ProxyResponse(
            status = resp.status.value,
            contentType = resp.headers["Content-Type"],
            location = resp.headers["Location"],
            body = resp.body<ByteArray>(),
        )

    /**
     * Deserializes the body of a [ProxyResponse] into [T].
     *
     * - **2xx** → decode JSON body into [T]
     * - **404** → throw [HttpNotFoundException] with the code/message
     *             produced by [exceptionMessageProvider] (format: `"code ; message"`)
     * - **other** → throw [ProxyResponseException] preserving upstream status
     *
     * Usage:
     * ```
     * val project: Project = deserializeDtoFromContract<Project>(proxy) {
     *     "project_not_found ; Project with id $projectId not found"
     * }
     * ```
     */
    inline fun <reified T : Any> deserializeDtoFromContract(
        proxyResponse: ProxyResponse,
        exceptionMessageProvider: () -> String,
    ): T {
        val content = proxyResponse.body?.decodeToString()

        return when {
            content != null && proxyResponse.isSuccess -> runCatching {
                json.decodeFromString<T>(content)
            }.getOrElse { cause ->
                throw InvalidUpstreamResponseException(
                    "Failed to deserialize upstream response: ${cause.message}",
                )
            }

            proxyResponse.status == HttpStatusCode.NotFound.value -> {
                val parts = exceptionMessageProvider().split(";", limit = 2)
                throw HttpNotFoundException(parts[0].trim(), parts.getOrElse(1) { "Not found" }.trim())
            }

            else -> {
                val message = content?.takeIf { it.isNotBlank() }
                    ?: "Upstream sent an incorrect response"
                throw ProxyResponseException(
                    Response.Status.fromStatusCode(proxyResponse.status)
                        ?: Response.Status.BAD_GATEWAY,
                    message,
                )
            }
        }
    }

    /**
     * Asserts that a [ProxyResponse] has a 2xx status.
     * Throws [ProxyResponseException] otherwise, preserving the upstream status.
     */
    @Suppress("unused")
    fun checkProxyResponse(proxyResponse: ProxyResponse, caller: String) {
        if (proxyResponse.isSuccess) return
        val message = proxyResponse.body?.decodeToString()?.takeIf { it.isNotBlank() }
            ?: "Request failed for caller $caller"
        throw ProxyResponseException(
            Response.Status.fromStatusCode(proxyResponse.status)
                ?: Response.Status.BAD_GATEWAY,
            message,
        )
    }

    /** Converts a [ProxyResponse] into a JAX-RS [Response], preserving upstream status. */
    fun ProxyResponse.toJakartaResponse(): Response =
        Response.status(status)
            .entity(bodyAsString())
            .type(contentType ?: MediaType.APPLICATION_JSON)
            .apply { location?.let { header(HttpHeaders.LOCATION, it) } }
            .build()

    /**
     * Executes [request] and translates infrastructure failures into
     * appropriate [ProxyResponse] status codes:
     *
     * - Timeout → 504 Gateway Timeout
     * - Connection refused → 502 Bad Gateway
     * - Ktor ResponseException → proxies upstream status faithfully
     * - [CancellationException] → re-thrown (cooperative cancellation)
     * - Anything else → 502 Bad Gateway
     */
    suspend inline fun tryProxyRequest(
        caller: String,
        crossinline request: suspend () -> ProxyResponse,
    ): ProxyResponse = runCatching {
        request()
    }.getOrElse { ex ->
        logger.error(
            "Proxy request failed for caller ({}) -> {} : {}",
            caller,
            ex::class.simpleName,
            ex.message,
        )
        when (ex) {
            is CancellationException -> throw ex

            is HttpRequestTimeoutException, is SocketTimeoutException ->
                ProxyResponse(
                    status = 504,
                    contentType = "application/json",
                    body = """{"error":"gateway_timeout","message":"upstream timed out"}""".toByteArray(),
                )

            is ConnectException ->
                ProxyResponse(
                    status = 502,
                    contentType = "application/json",
                    body = """{"error":"bad_gateway","message":"cannot reach upstream"}""".toByteArray(),
                )

            is ResponseException -> runCatching {
                val er = ex.response
                ProxyResponse(
                    status = er.status.value,
                    contentType = er.headers["Content-Type"],
                    body = er.body<ByteArray>(),
                )
            }.getOrElse {
                ProxyResponse(
                    status = 502,
                    contentType = "application/json",
                    body = """{"error":"bad_gateway"}""".toByteArray(),
                )
            }

            else -> ProxyResponse(
                status = 502,
                contentType = "application/json",
                body = """{"error":"bad_gateway"}""".toByteArray(),
            )
        }
    }

    fun normalizedUri(uri: String): String = uri.trimEnd('/')

    @Suppress("unused")
    fun normalizedPath(path: String?): String {
        val trimmed = path?.trim().orEmpty()
        if (trimmed.isBlank()) return ""
        return "/" + trimmed.trim('/')
    }
}
