package org.radarbase.user.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val email: String,
    /**
     * Must be "researcher" or "admin".
     */
    val kind: String = "researcher",
    /**
     * Kratos identity state, e.g. "active" / "inactive".
     * If null, Kratos default will be used.
     */
    val state: String? = null,
)
