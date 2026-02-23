package org.radarbase.delegate.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class User(
    val id: String,
    val projectId: String,
    val name: String,
    val email: String,
    val status: String,
    val metadata: Map<String, JsonElement> = emptyMap(),
)
