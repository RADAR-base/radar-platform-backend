package org.radarbase.user.model

import kotlinx.serialization.Serializable

@Serializable
data class Participant(
    val id: String,
    val projectId: String,
    val name: String,
    val email: String,
    val status: String,
    val sources: List<RadarSource> = emptyList(),
    val studyGroup: String,
    val enrollmentDate: String,
    val attributes: Map<String, String> = emptyMap(),
    val dateOfBirth: String?,
    val personName: String?,
    val radarInfo: RadarUser? = null,
    val kratosInfo: KratosUser? = null,
    val mpLogin: String? = null,
)
