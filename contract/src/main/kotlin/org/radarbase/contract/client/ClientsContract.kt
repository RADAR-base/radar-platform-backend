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

package org.radarbase.contract.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import org.radarbase.contract.utils.ContractUtils
import org.slf4j.LoggerFactory
import java.net.ConnectException
import java.util.concurrent.ConcurrentHashMap

/**
 * Central registry that manages one pre-configured [HttpClient] per
 * downstream microservice.
 *
 * Must be initialized (via [registerService]) before any contract object
 * attempts to retrieve a client. Typically called once at application
 * start-up from the delegate enhancer or main function.
 */
object ClientsContract {
    private val logger = LoggerFactory.getLogger(ClientsContract::class.java)
    private val clients = ConcurrentHashMap<String, HttpClient>()

    /**
     * Creates and registers an [HttpClient] for [serviceName] pointing
     * at [baseUrl]. Safe to call multiple times, later calls for
     * the same [serviceName] will close the previous client first.
     */
    fun registerService(
        serviceName: String,
        baseUrl: String,
        timeoutSeconds: Long = 30L,
        maxRetries: Int = 3,
    ) {
        clients.put(serviceName, buildClient(baseUrl, timeoutSeconds, maxRetries))
            ?.also { old ->
                logger.debug("Replacing existing client for service {}", serviceName)
                old.close()
            }
        logger.info("Registered contract client for service {} -> {}", serviceName, baseUrl)
    }

    /**
     * Returns the [HttpClient] registered for [serviceName].
     *
     * @throws IllegalStateException if [registerService] was not called for this name.
     */
    fun retrieveClientForService(serviceName: String): HttpClient =
        requireNotNull(clients[serviceName]) {
            "No contract client registered for service '$serviceName'. " +
                "Call ClientsContract.registerService() at application start-up."
        }

    /** Closes all managed clients. Call on application shutdown. */
    fun closeAll() {
        clients.forEach { (name, client) ->
            logger.debug("Closing contract client for {}", name)
            client.close()
        }
        clients.clear()
    }

    private fun buildClient(
        baseUrl: String,
        timeoutSeconds: Long,
        maxRetries: Int,
    ): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(ContractUtils.json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = timeoutSeconds * 1_000
            connectTimeoutMillis = timeoutSeconds * 1_000
        }
        install(HttpRequestRetry) {
            this.maxRetries = maxRetries
            retryOnExceptionIf { _, cause ->
                cause is HttpRequestTimeoutException || cause is ConnectException
            }
        }
        defaultRequest {
            url(ContractUtils.normalizedUri(baseUrl) + "/")
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }
    }
}

