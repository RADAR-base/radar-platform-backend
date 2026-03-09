package org.radarbase.core.model.project

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val email: String,
    val resources: List<String>,
)
