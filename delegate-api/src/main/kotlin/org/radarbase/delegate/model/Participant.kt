package org.radarbase.delegate.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Participant(
    val id: String,
    val projectId: String,
    val name: String,
    val email: String,
    val status: String,
    val sources: List<JsonElement> = emptyList(),
    val studyGroup: String,
    val enrollmentDate: String,
    val dateOfBirth: String?,
    val personName: String?,
    val attributes: Map<String, String> = emptyMap(),
    val mpLogin: String? = null,
)
