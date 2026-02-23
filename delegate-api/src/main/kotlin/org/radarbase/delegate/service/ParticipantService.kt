package org.radarbase.delegate.service

import org.radarbase.delegate.model.Participant

interface ParticipantService {
    suspend fun getParticipants(
        projectId: String,
        authToken: String?,
    ): List<Participant>

    suspend fun getParticipant(
        projectId: String,
        participantId: String,
        authToken: String?,
    ): Participant?

    suspend fun createParticipant(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String>

    suspend fun updateParticipant(
        projectId: String,
        participantId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String>
}
