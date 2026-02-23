package org.radarbase.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class KratosUser(
    @SerialName("id") val id: String,
    @SerialName("schema_id") val schemaId: String,
    @SerialName("schema_url") val schemaUrl: String,
    @SerialName("state") val state: String,
    @SerialName("state_changed_at") val stateChangedAt: String? = null,
    @SerialName("traits") val traits: KratosTraits,
    @SerialName("verifiable_addresses") val verifiableAddresses: List<KratosVerifiableAddress>,
    @SerialName("recovery_addresses") val recoveryAddresses: List<KratosRecoveryAddress>,
    @SerialName("metadata_public") val metadataPublic: KratosMetadataPublic?,
    @SerialName("metadata_admin") val metadataAdmin: JsonObject?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("organization_id") val organizationId: String?,
)

@Serializable
data class KratosTraits(
    @SerialName("email") val email: String?,
    @SerialName("projects") val projects: List<KratosProject> = emptyList(),
)

@Serializable
data class KratosProject(
    @SerialName("id") val id: String?,
    @SerialName("name") val name: String?,
    @SerialName("userId") val userId: String,
    @SerialName("consent") val consent: JsonObject? = null,
    @SerialName("additional") val additional: JsonObject? = null,
    @SerialName("eligibility") val eligibility: JsonObject? = null,
)

@Serializable
data class KratosVerifiableAddress(
    @SerialName("id") val id: String,
    @SerialName("value") val value: String,
    @SerialName("verified") val verified: Boolean,
    @SerialName("via") val via: String,
    @SerialName("status") val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class KratosRecoveryAddress(
    @SerialName("id") val id: String,
    @SerialName("value") val value: String,
    @SerialName("via") val via: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class KratosMetadataPublic(
    @SerialName("roles") val roles: List<String> = emptyList(),
    @SerialName("scope") val scope: List<String> = emptyList(),
    @SerialName("mp_login") val mpLogin: String,
    @SerialName("authorities") val authorities: List<String> = emptyList(),
)

@Serializable
data class KratosStudyData(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("time") val time: Long,
    @SerialName("answers") val answers: Map<String, String>,
    @SerialName("version") val version: String,
)
