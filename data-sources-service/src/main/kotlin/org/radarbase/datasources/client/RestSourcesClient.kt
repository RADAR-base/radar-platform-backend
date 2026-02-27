package org.radarbase.datasources.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.radarbase.core.util.ServiceTokenProvider
import org.radarbase.datasources.config.DataSourcesServiceConfig
import org.radarbase.datasources.model.RestSourceUser
import org.slf4j.LoggerFactory

@Singleton
class RestSourcesClient @Inject constructor(
    config: DataSourcesServiceConfig,
    private val tokenProvider: ServiceTokenProvider,
) {
    private val logger = LoggerFactory.getLogger(RestSourcesClient::class.java)
    private val restSourcesConfig = config.restSources
    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    private val client: HttpClient =
        HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = restSourcesConfig.timeoutSeconds * 1_000
            }
        }

    suspend fun getUsers(
        projectId: String,
        participantId: String,
        authorized: Boolean = true,
    ): List<RestSourceUser> {
        logger.debug(
            "Fetching REST sources users for project {}, participantId {}, authorized {}",
            projectId,
            participantId,
            authorized,
        )

        return try {
            val token = tokenProvider.getToken()

            val response: HttpResponse =
                client.get {
                    url(restSourcesConfig.baseUrl + restSourcesConfig.endpoints.users)
                    parameter("authorized", authorized)
                    parameter("search", participantId)
                    parameter("project-id", projectId)
                    header(HttpHeaders.Authorization, "Bearer $token")
                }

            // Check HTTP status code
            if (response.status.value !in 200..299) {
                logger.warn(
                    "REST sources API returned non-success status {} for project {}, participant {}",
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
                    "REST sources API returned error response for project {}, participant {}: {}",
                    projectId,
                    participantId,
                    responseText,
                )
                return emptyList()
            }

            val wrapper = json.decodeFromString<RestSourcesUsersResponse>(responseText)
            return wrapper.users
        } catch (e: kotlinx.serialization.SerializationException) {
            logger.warn(
                "Failed to deserialize REST sources response for project {}, participant {}: {}",
                projectId,
                participantId,
                e.message,
            )
            emptyList()
        } catch (e: Exception) {
            logger.info(
                "Failed to fetch REST sources users for project {}, participant {}",
                projectId,
                participantId,
                e,
            )
            emptyList()
        }
    }

    @Serializable
    private data class RestSourcesUsersResponse(
        @SerialName("users") val users: List<RestSourceUser> = emptyList(),
    )
}
