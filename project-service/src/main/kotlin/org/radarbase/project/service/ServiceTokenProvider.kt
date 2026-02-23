package org.radarbase.project.service

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.radarbase.project.config.ProjectServiceConfiguration
import org.slf4j.LoggerFactory
import java.time.Instant

@Singleton
class ServiceTokenProvider
    @Inject
    constructor(
        private val config: ProjectServiceConfiguration,
    ) {
        private val logger = LoggerFactory.getLogger(ServiceTokenProvider::class.java)
        private val mutex = Mutex()
        private var cachedToken: CachedToken? = null
        private val serviceAuth = config.serviceAuth

        private val client: HttpClient =
            HttpClient(CIO) {
                install(ContentNegotiation) {
                    json()
                }
            }

        @Serializable
        private data class TokenResponse(
            val access_token: String,
            val token_type: String = "Bearer",
            val expires_in: Long,
            val scope: String,
        )

        private data class CachedToken(
            val token: String,
            val expiresAt: Instant,
        )

        suspend fun getToken(): String {
            return mutex.withLock {
                val cached = cachedToken
                if (cached != null && Instant.now().isBefore(cached.expiresAt.minusSeconds(60))) {
                    return@withLock cached.token
                }

                logger.debug("Obtaining new service token from ${serviceAuth.tokenEndpoint}")
                val response =
                    client.post(serviceAuth.tokenEndpoint) {
                        contentType(ContentType.Application.FormUrlEncoded)
                        setBody(
                            FormDataContent(
                                Parameters.build {
                                    append("grant_type", "client_credentials")
                                    append("client_id", serviceAuth.clientId)
                                    append("client_secret", serviceAuth.clientSecret)
                                    append("audience", "res_ManagementPortal")
                                    serviceAuth.scope?.let { append("scope", it) }
                                },
                            ),
                        )
                    }

                val tokenResponse = Json.decodeFromString<TokenResponse>(response.bodyAsText())
                val expiresAt = Instant.now().plusSeconds(tokenResponse.expires_in)

                cachedToken =
                    CachedToken(
                        token = tokenResponse.access_token,
                        expiresAt = expiresAt,
                    )

                logger.debug("Service token obtained, expires at $expiresAt")
                tokenResponse.access_token
            }
        }
    }
