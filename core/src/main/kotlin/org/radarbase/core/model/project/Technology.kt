package org.radarbase.core.model.project

import kotlinx.serialization.Serializable

@Serializable
data class Technology(
    val devices: List<String>,
    val dataTypes: List<String>,
    val frequency: String,
    val notes: String?,
)
