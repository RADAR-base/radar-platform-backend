package org.radarbase.delegate.service

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.radarbase.delegate.config.DelegateConfig
import org.slf4j.LoggerFactory

@Singleton
class DataSourcesServiceImpl
    @Inject
    constructor(
        private val httpClient: HttpClientService,
        private val config: DelegateConfig,
    ) : DataSourcesService {
        private val logger = LoggerFactory.getLogger(DataSourcesServiceImpl::class.java)
        private val baseUrl = config.dataSourcesService.baseUrl

        override suspend fun getSourceCatalog(): String {
            logger.debug("Fetching source catalog from data-sources-service")
            return httpClient.get(
                "$baseUrl/sources",
                { it },
                authToken = null,
            )
        }

        override suspend fun getParticipantSources(
            projectId: String,
            participantId: String,
        ): String {
            logger.debug("Fetching participant sources for project {} and participant {}", projectId, participantId)

            return httpClient.get(
                "$baseUrl/projects/$projectId/participants/$participantId/sources",
                { it },
                authToken = null,
            )
        }
    }
