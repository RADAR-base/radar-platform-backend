package org.radarbase.delegate.service

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.radarbase.delegate.config.DelegateConfig
import org.radarbase.delegate.model.Participant
import org.slf4j.LoggerFactory

@Singleton
class ParticipantServiceImpl
@Inject
constructor(
    private val httpClient: HttpClientService,
    private val config: DelegateConfig,
) : ParticipantService {
    private val logger = LoggerFactory.getLogger(ParticipantServiceImpl::class.java)
    private val baseUrl = config.participantService.baseUrl

    override suspend fun getParticipants(
        projectId: String,
        authToken: String?,
    ): List<Participant> {
        logger.debug("Fetching participants for project $projectId")
        val url = "$baseUrl/project/$projectId/participants"
        return httpClient.getJson(url, authToken)
    }

    override suspend fun getParticipant(
        projectId: String,
        participantId: String,
        authToken: String?,
    ): Participant? {
        logger.debug("Fetching participant $participantId for project $projectId")
        return httpClient.getJson("$baseUrl/project/$projectId/participants/$participantId", authToken)
    }

    override suspend fun createParticipant(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String> {
        logger.debug("Creating participant for project $projectId")
        return httpClient.postWithBody("$baseUrl/project/$projectId/participants", body, authToken)
    }

    override suspend fun updateParticipant(
        projectId: String,
        participantId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String> {
        logger.debug("Updating participant $participantId for project $projectId")
        return httpClient.putWithBody("$baseUrl/project/$projectId/participants/$participantId", body, authToken)
    }
}
