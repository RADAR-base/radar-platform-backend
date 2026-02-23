package org.radarbase.user.model

import kotlinx.serialization.Serializable

/**
 * Request DTO for updating a user or participant
 * Fields are optional to support partial updates
 */
@Serializable
data class UpdateUserRequest(
    val email: String? = null,
    val status: String? = null,
    val studyGroup: String? = null,
    val attributes: Map<String, String>? = null,
    val dateOfBirth: String? = null,
    val personName: String? = null,
    val state: String? = null, // Kratos state (active, inactive, etc.)
    val traits: KratosTraits? = null, // For updating Kratos traits
    val metadataPublic: KratosMetadataPublic? = null, // For updating Kratos public metadata
)
