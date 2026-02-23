package org.radarbase.delegate.service

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.radarbase.delegate.config.DelegateConfig
import org.slf4j.LoggerFactory

@Singleton
class ConfigServiceImpl
    @Inject
    constructor(
        private val httpClient: HttpClientService,
        private val config: DelegateConfig,
    ) : ConfigService {
        private val logger = LoggerFactory.getLogger(ConfigServiceImpl::class.java)
        private val baseUrl = config.configService.baseUrl

        override suspend fun getStudyConfig(
            projectId: String,
            authToken: String?,
        ): String {
            logger.debug("Fetching study config for project $projectId")
            return httpClient.get("$baseUrl/projects/$projectId", { it }, authToken)
        }

        override suspend fun getProtocol(
            projectId: String,
            authToken: String?,
        ): String {
            logger.debug("Fetching protocol for project $projectId")
            return httpClient.get("$baseUrl/projects/$projectId/sources/protocol", { it }, authToken)
        }

        override suspend fun getEnrolmentSource(
            projectId: String,
            authToken: String?,
        ): String {
            logger.debug("Fetching enrolment source for project $projectId")
            return httpClient.get("$baseUrl/projects/$projectId/sources/enrolment", { it }, authToken)
        }

        override suspend fun getEnrolmentLanding(
            projectId: String,
            authToken: String?,
        ): String {
            logger.debug("Fetching enrolment landing for project $projectId")
            return httpClient.get("$baseUrl/projects/$projectId/enrolment/landing", { it }, authToken)
        }

        override suspend fun getEnrolmentProtocol(
            projectId: String,
            authToken: String?,
        ): String {
            logger.debug("Fetching enrolment protocol for project $projectId")
            return httpClient.get("$baseUrl/projects/$projectId/enrolment/protocol", { it }, authToken)
        }

        override suspend fun getQuestionnaires(
            projectId: String,
            authToken: String?,
        ): String {
            logger.debug("Fetching questionnaires for project $projectId")
            return httpClient.get("$baseUrl/projects/$projectId/questionnaires", { it }, authToken)
        }

        override suspend fun getQuestionnaire(
            projectId: String,
            questionnaireId: String,
            authToken: String?,
        ): String {
            logger.debug("Fetching questionnaire $questionnaireId for project $projectId")
            return httpClient.get(
                "$baseUrl/projects/$projectId/questionnaires/$questionnaireId",
                { it },
                authToken,
            )
        }

        override suspend fun updateStudyConfig(
            projectId: String,
            body: String,
            authToken: String?,
        ): Pair<Int, String> {
            logger.info("PUT config/projects/$projectId -> config-service")
            val result = httpClient.putWithBody("$baseUrl/projects/$projectId", body, authToken)
            logger.info("PUT config/projects/$projectId response: status={}, body={}", result.first, truncate(result.second, 500))
            return result
        }

        override suspend fun updateQuestionnaires(
            projectId: String,
            body: String,
            authToken: String?,
        ): Pair<Int, String> {
            logger.info("PUT config/projects/$projectId/questionnaires -> config-service")
            val result = httpClient.putWithBody("$baseUrl/projects/$projectId/questionnaires", body, authToken)
            logger.info("PUT questionnaires response: status={}, body={}", result.first, truncate(result.second, 500))
            return result
        }

        override suspend fun createQuestionnaire(
            projectId: String,
            body: String,
            authToken: String?,
        ): Pair<Int, String> {
            logger.info("POST config/projects/$projectId/questionnaires -> config-service")
            val result = httpClient.postWithBody("$baseUrl/projects/$projectId/questionnaires", body, authToken)
            logger.info("POST questionnaire response: status={}, body={}", result.first, truncate(result.second, 500))
            return result
        }

        override suspend fun updateQuestionnaire(
            projectId: String,
            questionnaireId: String,
            body: String,
            authToken: String?,
        ): Pair<Int, String> {
            logger.info("PUT config/projects/$projectId/questionnaires/$questionnaireId -> config-service")
            val result =
                httpClient.putWithBody(
                    "$baseUrl/projects/$projectId/questionnaires/$questionnaireId",
                    body,
                    authToken,
                )
            logger.info("PUT questionnaire response: status={}, body={}", result.first, truncate(result.second, 500))
            return result
        }

        override suspend fun deleteQuestionnaire(
            projectId: String,
            questionnaireId: String,
            authToken: String?,
        ): Pair<Int, String> {
            logger.info("DELETE config/projects/$projectId/questionnaires/$questionnaireId -> config-service")
            val result =
                httpClient.delete(
                    "$baseUrl/projects/$projectId/questionnaires/$questionnaireId",
                    authToken,
                )
            logger.info("DELETE questionnaire response: status={}, body={}", result.first, truncate(result.second, 500))
            return result
        }

        override suspend fun updateProtocolSource(
            projectId: String,
            body: String,
            authToken: String?,
        ): Pair<Int, String> {
            logger.info("PUT config/projects/$projectId/sources/protocol -> config-service")
            val result =
                httpClient.putWithBody(
                    "$baseUrl/projects/$projectId/sources/protocol",
                    body,
                    authToken,
                )
            logger.info("PUT protocol source response: status={}, body={}", result.first, truncate(result.second, 500))
            return result
        }

        override suspend fun updateProtocolBody(
            projectId: String,
            body: String,
            authToken: String?,
        ): Pair<Int, String> {
            logger.info("PUT config/projects/$projectId/protocol -> config-service")
            val result = httpClient.putWithBody("$baseUrl/projects/$projectId/protocol", body, authToken)
            logger.info("PUT protocol body response: status={}, body={}", result.first, truncate(result.second, 500))
            return result
        }

        override suspend fun updateEnrolmentSource(
            projectId: String,
            body: String,
            authToken: String?,
        ): Pair<Int, String> {
            logger.info("PUT config/projects/$projectId/sources/enrolment -> config-service")
            val result =
                httpClient.putWithBody(
                    "$baseUrl/projects/$projectId/sources/enrolment",
                    body,
                    authToken,
                )
            logger.info("PUT enrolment source response: status={}, body={}", result.first, truncate(result.second, 500))
            return result
        }

        override suspend fun updateEnrolmentLandingBody(
            projectId: String,
            body: String,
            authToken: String?,
        ): Pair<Int, String> {
            logger.info("PUT config/projects/$projectId/enrolment/landing -> config-service")
            val result =
                httpClient.putWithBody(
                    "$baseUrl/projects/$projectId/enrolment/landing",
                    body,
                    authToken,
                )
            logger.info("PUT enrolment landing response: status={}, body={}", result.first, truncate(result.second, 500))
            return result
        }

        override suspend fun updateEnrolmentProtocolBody(
            projectId: String,
            body: String,
            authToken: String?,
        ): Pair<Int, String> {
            logger.info("PUT config/projects/$projectId/enrolment/protocol -> config-service")
            val result =
                httpClient.putWithBody(
                    "$baseUrl/projects/$projectId/enrolment/protocol",
                    body,
                    authToken,
                )
            logger.info("PUT enrolment protocol response: status={}, body={}", result.first, truncate(result.second, 500))
            return result
        }

        private fun truncate(
            s: String,
            maxLen: Int,
        ): String = if (s.length <= maxLen) s else s.take(maxLen) + "... (${s.length} chars total)"
    }
