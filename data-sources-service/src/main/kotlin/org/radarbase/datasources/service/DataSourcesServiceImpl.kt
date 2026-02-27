package org.radarbase.datasources.service

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.radarbase.datasources.client.RadarSourcesClient
import org.radarbase.datasources.client.RestSourcesClient
import org.radarbase.datasources.model.CombinedParticipantSource
import org.slf4j.LoggerFactory

@Singleton
class DataSourcesServiceImpl
@Inject
constructor(
    private val radarSourcesClient: RadarSourcesClient,
    private val restSourcesClient: RestSourcesClient,
) : DataSourcesService {
    private val logger = LoggerFactory.getLogger(DataSourcesServiceImpl::class.java)
    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    override suspend fun getParticipantSources(
        projectId: String,
        participantId: String,
    ): String {
        // Fetch sources independently - if one fails, the other can still succeed
        val restSources =
            try {
                restSourcesClient.getUsers(projectId, participantId)
            } catch (e: Exception) {
                logger.warn(
                    "Failed to fetch REST sources for project {}, participant {}, " +
                        "continuing with RADAR sources only: {}",
                    projectId,
                    participantId,
                    e,
                )
                emptyList()
            }

        val radarSources =
            try {
                radarSourcesClient.getParticipantSources(projectId, participantId)
            } catch (e: Exception) {
                logger.warn(
                    "Failed to fetch RADAR sources for project {}, participant {}, " +
                        "continuing with REST sources only: {}",
                    projectId,
                    participantId,
                    e,
                )
                emptyList()
            }

        val combinedRest =
            restSources.map { rest ->
                CombinedParticipantSource(
                    projectId = rest.projectId,
                    participantId = rest.userId,
                    sourceId = rest.sourceId,
                    sourceName = rest.sourceType + "-" + rest.serviceUserId,
                    humanReadableUserId = rest.humanReadableUserId,
                    externalId = rest.externalId,
                    sourceType = rest.sourceType,
                    sourceSystem = "REST",
                    isAuthorized = rest.isAuthorized,
                    hasValidToken = rest.hasValidToken,
                    createdAt = rest.createdAt,
                )
            }

        val combinedRadar =
            radarSources.map { radar ->
                CombinedParticipantSource(
                    projectId = projectId,
                    participantId = participantId,
                    sourceId = radar.sourceId,
                    sourceName = radar.sourceName,
                    humanReadableUserId = null,
                    externalId = null,
                    sourceType = radar.sourceTypeModel,
                    sourceSystem = "RADAR",
                    isAuthorized = null,
                    hasValidToken = null,
                    createdAt = null,
                )
            }

        val combined = combinedRest + combinedRadar

        return json.encodeToString(combined)
    }

    override suspend fun getRestSourcesUsers(
        projectId: String,
        search: String,
        authorized: Boolean,
    ): String = json.encodeToString(restSourcesClient.getUsers(projectId, search, authorized))
}
