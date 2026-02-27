package org.radarbase.core.model.project

import kotlinx.serialization.Serializable

@Serializable
data class Domain(
    val area: String,
    val keywords: List<String>,
)
