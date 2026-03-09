package org.radarbase.core.model.project

import kotlinx.serialization.Serializable

@Serializable
data class Eligibility(
    val inclusion: List<String>,
    val exclusion: List<String>,
)
