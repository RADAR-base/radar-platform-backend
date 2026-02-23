package org.radarbase.user.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateParticipantRequest(
    val email: String,
    /**
     * Required by Management Portal webhook: identity.traits.projects[0].userId
     * (a.k.a. subject login / projectUserId).
     */
    val projectUserId: String,
    /**
     * Kratos identity state, e.g. "active" / "inactive".
     * If null, Kratos default will be used.
     */
    val state: String? = null,
)
