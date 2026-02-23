package org.radarbase.user.client

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.radarbase.user.config.UserServiceConfig
import org.radarbase.user.model.KratosSubjectWebhookDTO
import org.radarbase.user.model.ManagementPortalProxyResponse
import org.radarbase.user.model.RadarUser
import org.radarbase.user.service.ServiceTokenProvider
import org.slf4j.LoggerFactory

@Singleton
class RadarClient
    @Inject
    constructor(
        private val config: UserServiceConfig,
        ktorClientFactory: KtorClientFactory,
        private val tokenProvider: ServiceTokenProvider,
    ) {
        private val logger = LoggerFactory.getLogger(RadarClient::class.java)
        private val radarConfig = config.managementPortal

        private val client =
            ktorClientFactory.createClient(
                baseUrl = radarConfig.baseUrl,
                timeoutSeconds = radarConfig.timeoutSeconds,
                maxRetriesCount = radarConfig.maxRetries,
            )

        suspend fun checkHealth(): Boolean {
            logger.debug("Checking Radar health")
            return try {
                val response =
                    client
                        .get {
                            url("${config.managementPortal.baseUrl}${config.managementPortal.endpoints.health}")
                            header("Accept", "application/json")
                        }.body<String>()

                // TODO: Check other components like db are UP too
                Json
                    .parseToJsonElement(response)
                    .jsonObject["status"]
                    ?.jsonPrimitive
                    ?.content == "UP"
            } catch (e: Exception) {
                logger.error("Failed to check Radar health", e)
                false
            }
        }

        suspend fun getUsers(projectId: String): List<RadarUser> {
            logger.debug("Fetching all users from RADAR for project $projectId")
            return try {
                val token = tokenProvider.getToken()
                client
                    .get {
                        url(
                            "${config.managementPortal.baseUrl}${
                                radarConfig.endpoints.users.replace(
                                    "{projectId}",
                                    projectId,
                                )
                            }",
                        )
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }.body()
            } catch (e: Exception) {
                logger.error("Failed to fetch users from RADAR for project $projectId", e)
                emptyList()
            }
        }

        /**
         * Create a (researcher/admin) user in Management Portal.
         * MP will create the Kratos identity automatically.
         *
         * We proxy a raw JSON body so the frontend can send exactly what MP expects.
         */
        suspend fun createUserRaw(
            projectId: String,
            jsonBody: String,
        ): ManagementPortalProxyResponse {
            logger.debug("Creating user in RADAR for project $projectId")
            return try {
                val token = tokenProvider.getToken()
                val response =
                    client.post {
                        url(
                            "${config.managementPortal.baseUrl}${
                                radarConfig.endpoints.users.replace(
                                    "{projectId}",
                                    projectId,
                                )
                            }",
                        )
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(jsonBody)
                    }
                ManagementPortalProxyResponse(
                    status = response.status.value,
                    body = response.bodyAsText().ifBlank { null },
                )
            } catch (e: Exception) {
                logger.error("Failed to create user in RADAR for project $projectId", e)
                ManagementPortalProxyResponse(
                    status = 502,
                    body = e.message,
                )
            }
        }

        suspend fun getUser(
            projectId: String,
            userId: String,
        ): RadarUser? {
            logger.debug("Fetching user $userId from RADAR for project $projectId")
            return try {
                val token = tokenProvider.getToken()
                client
                    .get {
                        url(
                            "${config.managementPortal.baseUrl}${
                                radarConfig.endpoints.users.replace(
                                    "{projectId}",
                                    projectId,
                                )
                            }" + "/$userId",
                        )
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }.body()
            } catch (e: Exception) {
                logger.error("Failed to fetch user $userId from RADAR for project $projectId", e)
                null
            }
        }

        suspend fun getParticipants(projectId: String): List<RadarUser> {
            logger.debug("Fetching all participants from RADAR for project $projectId")
            return try {
                val token = tokenProvider.getToken()
                client
                    .get {
                        url(
                            "${config.managementPortal.baseUrl}${
                                radarConfig.endpoints.participants.replace(
                                    "{projectId}",
                                    projectId,
                                )
                            }?size=1000",
                        )
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }.body()
            } catch (e: Exception) {
                logger.error("Failed to fetch participants from RADAR for project $projectId", e)
                emptyList()
            }
        }

        suspend fun getParticipant(
            projectId: String,
            participantId: String,
        ): RadarUser? {
            logger.debug("Fetching participant $participantId from RADAR for project $projectId")
            return try {
                val token = tokenProvider.getToken()
                client
                    .get {
                        url(
                            "${config.managementPortal.baseUrl}${
                                radarConfig.endpoints.participant
                            }" + "/$participantId",
                        )
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }.body()
            } catch (e: Exception) {
                logger.error("Failed to fetch participant $participantId from RADAR for project $projectId", e)
                null
            }
        }

        /**
         * Update a user in Radar Management Portal
         * @param projectId The project ID
         * @param userId The user ID
         * @param updatedUser The updated user object
         * @return The updated user or null if update failed
         */
        suspend fun updateUser(
            projectId: String,
            userId: String,
            updatedUser: RadarUser,
        ): RadarUser? {
            logger.debug("Updating user $userId in RADAR for project $projectId")
            return try {
                val token = tokenProvider.getToken()
                client
                    .put {
                        url(
                            "${config.managementPortal.baseUrl}${
                                radarConfig.endpoints.users.replace(
                                    "{projectId}",
                                    projectId,
                                )
                            }" + "/$userId",
                        )
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(updatedUser)
                    }.body<RadarUser>()
            } catch (e: Exception) {
                logger.error("Failed to update user $userId in RADAR for project $projectId", e)
                null
            }
        }

        /**
         * Update a participant in Radar Management Portal
         * @param projectId The project ID
         * @param participantId The participant ID
         * @param updatedParticipant The updated participant object
         * @return The updated participant or null if update failed
         */
        suspend fun updateParticipant(
            projectId: String,
            participantId: String,
            updatedParticipant: RadarUser,
        ): RadarUser? {
            logger.debug("Updating participant $participantId in RADAR for project $projectId")
            return try {
                val token = tokenProvider.getToken()
                client
                    .put {
                        url(
                            "${config.managementPortal.baseUrl}${
                                radarConfig.endpoints.participant
                            }",
                        )
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(updatedParticipant)
                    }.body<RadarUser>()
            } catch (e: Exception) {
                logger.error("Failed to update participant $participantId in RADAR for project $projectId", e)
                null
            }
        }

        /**
         * Manually call the Management Portal Kratos subjects webhook.
         * This is needed when identities are created via the Kratos admin API (webhook may not fire).
         *
         * @return true if the call succeeded, false otherwise
         */
        suspend fun callKratosSubjectsWebhook(payload: KratosSubjectWebhookDTO): Boolean {
            logger.debug("Calling Management Portal Kratos subjects webhook for identity {}", payload.identity.id)
            return try {
                val token = tokenProvider.getToken()
                val response =
                    client.post {
                        url("${config.managementPortal.baseUrl}${radarConfig.endpoints.kratosSubjectsWebhook}")
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(payload)
                    }
                response.status.value in 200..299
            } catch (e: Exception) {
                logger.error(
                    "Failed to call Management Portal Kratos subjects webhook for identity ${payload.identity.id}",
                    e,
                )
                false
            }
        }
    }
