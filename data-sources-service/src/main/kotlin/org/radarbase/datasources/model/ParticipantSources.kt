package org.radarbase.datasources.model

import kotlinx.serialization.Serializable

@Serializable
data class RestSourceUser(
    val id: String,
    val createdAt: String,
    val projectId: String,
    val userId: String,
    val humanReadableUserId: String? = null,
    val externalId: String? = null,
    val sourceId: String,
    val serviceUserId: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val sourceType: String? = null,
    val isAuthorized: Boolean = false,
    val hasValidToken: Boolean = false,
    val version: String? = null,
    val timesReset: Int? = null,
)

@Serializable
data class RadarParticipantSource(
    val id: Long,
    val sourceId: String,
    val sourceName: String? = null,
    val sourceTypeCatalogVersion: String? = null,
    val sourceTypeId: Long? = null,
    val sourceTypeModel: String? = null,
    val sourceTypeProducer: String? = null,
    val assigned: Boolean? = null,
    val expectedSourceName: String? = null,
)

@Serializable
data class CombinedParticipantSource(
    val projectId: String,
    val participantId: String,
    val sourceId: String,
    val sourceName: String? = null,
    val humanReadableUserId: String? = null,
    val externalId: String? = null,
    val sourceType: String? = null,
    val sourceSystem: String,
    val isAuthorized: Boolean? = null,
    val hasValidToken: Boolean? = null,
    val createdAt: String? = null,
)
