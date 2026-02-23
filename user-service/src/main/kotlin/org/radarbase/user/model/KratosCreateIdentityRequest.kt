package org.radarbase.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class KratosCreateIdentityRequest(
    @SerialName("schema_id") val schemaId: String,
    @SerialName("traits") val traits: KratosTraits,
    @SerialName("state") val state: String? = null,
    @SerialName("metadata_public") val metadataPublic: KratosMetadataPublic? = null,
    @SerialName("metadata_admin") val metadataAdmin: JsonObject? = null,
)
