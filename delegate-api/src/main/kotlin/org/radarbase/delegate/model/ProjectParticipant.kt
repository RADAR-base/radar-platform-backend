package org.radarbase.delegate.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ProjectParticipant(
    val participantId: String,
    val projectId: String,
    val status: String,
    val enrollmentDate: String? = null,
    val attributes: Map<String, String> = emptyMap(),
    val metadata: Map<String, JsonElement> = emptyMap(),
)
