package org.radarbase.project.model

import kotlinx.serialization.Serializable

/** Request body for creating a group. */
@Serializable
data class CreateGroupRequest(
    val name: String,
)

/** Group DTO returned by Management Portal groups API. */
@Serializable
data class Group(
    val name: String,
)
