package org.radarbase.core.model.project

import kotlinx.serialization.Serializable

@Serializable
data class Population(
    val ageRange: AgeRange,
    val maxParticipants: Int,
)

@Serializable
data class AgeRange(
    val min: Int,
    val max: Int,
)
