package org.radarbase.user.model

data class User(
    val id: String,
    val projectId: String,
    val name: String,
    val email: String,
    val status: String,
    val metadata: Map<String, Any> = emptyMap(),
)
