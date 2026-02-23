package org.radarbase.project.model

import kotlinx.serialization.Serializable

@Serializable
data class RadarProject(
    val id: Long,
    val projectName: String,
    val humanReadableProjectName: String? = null,
    val description: String? = null,
    val projectStatus: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val organization: RadarOrganization? = null,
    val location: String? = null,
    val persistentTokenTimeout: Long? = null,
    val attributes: Map<String, String> = emptyMap(),
)

@Serializable
data class RadarOrganization(
    val id: Long,
    val name: String,
    val description: String? = null,
)
