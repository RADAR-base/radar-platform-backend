package org.radarbase.project.model

import kotlinx.serialization.Serializable

@Serializable
data class ProjectParticipant(
    val participantId: String,
    val projectId: String,
    val status: String,
    val enrollmentDate: String? = null,
    val attributes: Map<String, String> = emptyMap(),
)