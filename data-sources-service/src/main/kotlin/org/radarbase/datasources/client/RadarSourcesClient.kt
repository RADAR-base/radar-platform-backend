package org.radarbase.datasources.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import org.radarbase.core.util.ServiceTokenProvider
import org.radarbase.datasources.config.DataSourcesServiceConfig
import org.radarbase.datasources.model.RadarParticipantSource
import org.slf4j.LoggerFactory

@Singleton
class RadarSourcesClient
    @Inject
    constructor(
        config: DataSourcesServiceConfig,
        private val tokenProvider: ServiceTokenProvider,
    ) {
        private val logger = LoggerFactory.getLogger(RadarSourcesClient::class.java)
        private val managementPortalConfig = config.managementPortal
        private val json =
            Json {
                ignoreUnknownKeys = true
            }

        private val client: HttpClient =
            HttpClient(CIO) {
                install(HttpTimeout) {
                    requestTimeoutMillis = managementPortalConfig.timeoutSeconds * 1_000
                }
            }

        suspend fun getParticipantSources(
            projectId: String,
            participantId: String,
        ): List<RadarParticipantSource> {
            logger.debug("Fetching sources for participant {} in project {}", participantId, projectId)
            return try {
                val token = tokenProvider.getToken()
                val url =
                    buildSourcesUrl(
                        participantId = participantId,
                    )

                val response: HttpResponse =
                    client.get {
                        url(url)
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }

                // Check HTTP status code
                if (response.status.value !in 200..299) {
                    logger.warn(
                        "Management Portal API returned non-success status {} for project {}, participant {}",
                        response.status,
                        projectId,
                        participantId,
                    )
                    return emptyList()
                }

                val responseText = response.bodyAsText()

                // Check if response is an error JSON before deserializing
                if (responseText.trimStart().startsWith("{\"error\"")) {
                    logger.warn(
                        "Management Portal API returned error response for project {}, participant {}: {}",
                        projectId,
                        participantId,
                        responseText,
                    )
                    return emptyList()
                }

                json.decodeFromString<List<RadarParticipantSource>>(responseText)
            } catch (e: kotlinx.serialization.SerializationException) {
                logger.warn(
                    "Failed to deserialize Management Portal response for project {}, participant {}: {}",
                    projectId,
                    participantId,
                    e.message,
                )
                emptyList()
            } catch (e: Exception) {
                logger.error(
                    "Failed to fetch participant sources from Management Portal for project {}, participant {}",
                    projectId,
                    participantId,
                    e,
                )
                emptyList()
            }
        }

        private fun buildSourcesUrl(participantId: String): String =
            managementPortalConfig.baseUrl +
                managementPortalConfig.endpoints.participantSources
                    .replace("{participantId}", participantId)
    }
