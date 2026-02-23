package org.radarbase.user.model

import kotlinx.serialization.Serializable

@Serializable
data class RadarUser(
    val id: Long,
    val externalLink: String?,
    val externalId: String?,
    val status: String,
    val group: String?,
    val dateOfBirth: String?,
    val enrollmentDate: String,
    val personName: String?,
    val roles: List<RadarRole>,
    val sources: List<RadarSource>,
    val attributes: Map<String, String>,
    val login: String,
    val identity: String? = null,
    val project: RadarProject? = null,
)

@Serializable
data class RadarProject(
    val id: Long,
    val projectName: String,
    val description: String? = null,
    val organization: RadarOrganization? = null,
    val organizationName: String? = null,
    val location: String? = null,
    val sourceTypes: List<RadarSourceType> = emptyList(),
    val attributes: Map<String, String> = emptyMap(),
    val groups: List<RadarProjectGroup> = emptyList(),
)

@Serializable
data class RadarOrganization(
    val id: Long,
    val name: String,
    val description: String? = null,
    val location: String? = null,
    val projects: List<RadarProject> = emptyList(),
)

@Serializable
data class RadarSourceType(
    val id: Long,
    val producer: String,
    val model: String,
    val catalogVersion: String,
    val sourceTypeScope: String? = null,
    val canRegisterDynamically: Boolean? = null,
)

@Serializable
data class RadarProjectGroup(
    val name: String,
)

@Serializable
data class RadarRole(
    val id: Long,
    val projectId: Long,
    val projectName: String,
    val authorityName: String,
)

@Serializable
data class RadarSource(
    val id: Long,
    val sourceTypeId: Long,
    val sourceTypeProducer: String,
    val sourceTypeModel: String,
    val sourceTypeCatalogVersion: String,
    val expectedSourceName: String?,
    val sourceId: String,
    val sourceName: String,
    val attributes: Map<String, String>,
    val assigned: Boolean,
)
