package org.radarbase.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KratosSubjectWebhookDTO(
    val identity: Identity,
    val cookies: Map<String, String>? = null,
) {
    @Serializable
    data class Identity(
        val id: String,
        @SerialName("schema_id") val schemaId: String? = null,
        val traits: Traits? = null,
    )

    @Serializable
    data class Traits(
        val email: String? = null,
        val projects: List<ProjectTrait>? = null,
    )

    @Serializable
    data class ProjectTrait(
        val id: String? = null,
        val userId: String? = null,
    )
}
