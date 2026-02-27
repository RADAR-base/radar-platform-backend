package org.radarbase.project.client

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
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
import org.radarbase.core.model.Group
import org.radarbase.core.util.KtorClientFactory
import org.radarbase.core.util.ServiceTokenProvider
import org.radarbase.project.config.ProjectServiceConfiguration
import org.radarbase.project.model.CreateGroupRequest
import org.radarbase.project.model.ProjectParticipant
import org.radarbase.project.model.RadarProject
import org.slf4j.LoggerFactory

@Singleton
class RadarProjectClient
    @Inject
    constructor(
        private val serviceTokenProvider: ServiceTokenProvider,
        config: ProjectServiceConfiguration,
        ktorClientFactory: KtorClientFactory,
    ) {
        private val logger = LoggerFactory.getLogger(RadarProjectClient::class.java)
        private val radarConfig = config.radar

        private val client = ktorClientFactory.createClient(
                baseUrl = radarConfig.baseUrl,
                timeoutSeconds = radarConfig.timeout,
                maxRetriesCount = radarConfig.maxRetries,
            )

        suspend fun checkHealth(): Boolean {
            logger.debug("Checking Radar health")
            return try {
                val response =
                    client
                        .get {
                            url("${radarConfig.baseUrl}/management/health")
                            header("Accept", "application/json")
                        }.body<String>()

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

        suspend fun getProjects(authToken: String? = null): List<RadarProject> {
            logger.info("Fetching all projects from RADAR")
            return try {
                val token = authToken ?: serviceTokenProvider.getToken()
                client
                    .get {
                        url("${radarConfig.baseUrl}/projects")
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }.body()
            } catch (e: Exception) {
                logger.error("Failed to fetch projects from RADAR", e)
                emptyList()
            }
        }

        suspend fun getProject(
            projectId: Long,
            authToken: String? = null,
        ): RadarProject? {
            logger.debug("Fetching project $projectId from RADAR")
            return try {
                val token = authToken ?: serviceTokenProvider.getToken()
                client
                    .get {
                        url("${radarConfig.baseUrl}/projects/$projectId")
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }.body()
            } catch (e: Exception) {
                logger.error("Failed to fetch project $projectId from RADAR", e)
                null
            }
        }

        suspend fun getProjectParticipants(
            projectId: Long,
            authToken: String? = null,
        ): List<ProjectParticipant> {
            logger.debug("Fetching participants for project $projectId from RADAR")
            return try {
                val token = authToken ?: serviceTokenProvider.getToken()
                client
                    .get {
                        url("${radarConfig.baseUrl}/projects/$projectId/participants")
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }.body()
            } catch (e: Exception) {
                logger.error("Failed to fetch participants for project $projectId from RADAR", e)
                emptyList()
            }
        }

        suspend fun getProjectParticipant(
            projectId: Long,
            participantId: String,
            authToken: String? = null,
        ): ProjectParticipant? {
            logger.debug("Fetching participant $participantId for project $projectId from RADAR")
            return try {
                val token = authToken ?: serviceTokenProvider.getToken()
                client
                    .get {
                        url("${radarConfig.baseUrl}/projects/$projectId/participants/$participantId")
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }.body()
            } catch (e: Exception) {
                logger.error(
                    "Failed to fetch participant $participantId for project $projectId from RADAR",
                    e,
                )
                null
            }
        }

        private fun groupsUrl(projectName: String) = "${radarConfig.baseUrl}/projects/$projectName/groups"

        /**
         * List groups for a project (Management Portal API).
         * @param projectName project name (e.g. "paprka")
         */
        suspend fun listGroups(
            projectName: String,
            authToken: String? = null,
        ): List<Group> {
            logger.debug("Listing groups for project $projectName from RADAR")
            return try {
                val token = authToken ?: serviceTokenProvider.getToken()
                client
                    .get {
                        url(groupsUrl(projectName))
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }.body()
            } catch (e: Exception) {
                logger.error("Failed to list groups for project $projectName from RADAR", e)
                emptyList()
            }
        }

        /**
         * Create a group (Management Portal API).
         * @param projectName project name
         * @param request body with "name": group name
         * @return created group or null on failure
         */
        suspend fun createGroup(
            projectName: String,
            request: CreateGroupRequest,
            authToken: String? = null,
        ): Group? {
            logger.debug("Creating group ${request.name} for project $projectName in RADAR")
            return try {
                val token = authToken ?: serviceTokenProvider.getToken()
                val response =
                    client.post {
                        url(groupsUrl(projectName))
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }
                response.bodyAsText() // consume body; MP may return 201 with empty/different body
                if (response.status.value in 200..299) Group(name = request.name) else null
            } catch (e: Exception) {
                logger.error(
                    "Failed to create group ${request.name} for project $projectName in RADAR",
                    e,
                )
                null
            }
        }

        /**
         * Delete a group (Management Portal API).
         * @param projectName project name
         * @param groupName group name
         * @param unlinkSubjects if true, unlink subjects from the group before deleting
         * @return true if delete succeeded (2xx)
         */
        suspend fun deleteGroup(
            projectName: String,
            groupName: String,
            unlinkSubjects: Boolean = false,
            authToken: String? = null,
        ): Boolean {
            logger.debug("Deleting group $groupName for project $projectName in RADAR")
            return try {
                val token = authToken ?: serviceTokenProvider.getToken()
                val deleteUrl =
                    if (unlinkSubjects) {
                        "${groupsUrl(projectName)}/$groupName?unlinkSubjects=true"
                    } else {
                        "${groupsUrl(projectName)}/$groupName"
                    }
                val response =
                    client.delete {
                        url(deleteUrl)
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                response.status.value in 200..299
            } catch (e: Exception) {
                logger.error(
                    "Failed to delete group $groupName for project $projectName in RADAR",
                    e,
                )
                false
            }
        }
    }
