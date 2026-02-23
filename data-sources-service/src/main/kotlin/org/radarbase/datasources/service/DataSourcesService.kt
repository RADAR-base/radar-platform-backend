package org.radarbase.datasources.service

interface DataSourcesService {
    suspend fun getParticipantSources(
        projectId: String,
        participantId: String,
    ): String

    suspend fun getRestSourcesUsers(
        projectId: String,
        search: String,
        authorized: Boolean = true,
    ): String
}
