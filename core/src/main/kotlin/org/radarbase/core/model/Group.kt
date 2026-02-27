package org.radarbase.core.model

import kotlinx.serialization.Serializable

/** Group DTO returned by Management Portal groups API. */
@Serializable
data class Group(
    val name: String,
)

