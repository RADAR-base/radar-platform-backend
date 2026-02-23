package org.radarbase.user.model

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors Management Portal's UserDTO for create/update user API.
 * MP creates the Kratos identity when a user is created.
 */
@Serializable
data class UserDTO(
    val id: Long? = null,
    val login: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    @SerialName("activated") @JsonProperty("activated") val isActivated: Boolean = false,
    val langKey: String? = null,
    val createdBy: String? = null,
    val createdDate: String? = null,
    val lastModifiedBy: String? = null,
    val lastModifiedDate: String? = null,
    val roles: List<RoleDTO>? = null,
    val authorities: List<String>? = null,
    val accessToken: String? = null,
    /** Identifier for association with the identity service provider (Kratos). Null if not linked. */
    val identity: String? = null,
)

@Serializable
data class RoleDTO(
    val id: Long? = null,
    val name: String? = null,
)
