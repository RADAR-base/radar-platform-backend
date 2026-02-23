package org.radarbase.user.model

import kotlinx.serialization.Serializable

@Serializable
data class TriggerRecoveryEmailRequest(
    val email: String,
    /**
     * Kratos recovery method. Common values: "link" (email link) or "code" (email code).
     * If omitted, defaults to "link".
     */
    val method: String? = null,
)
