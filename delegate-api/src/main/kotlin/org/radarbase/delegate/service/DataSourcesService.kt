package org.radarbase.delegate.service

interface DataSourcesService {
    suspend fun getSourceCatalog(): String

    suspend fun getParticipantSources(
        projectId: String,
        participantId: String,
    ): String
}
