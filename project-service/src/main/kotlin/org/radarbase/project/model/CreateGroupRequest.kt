package org.radarbase.project.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String,
)
