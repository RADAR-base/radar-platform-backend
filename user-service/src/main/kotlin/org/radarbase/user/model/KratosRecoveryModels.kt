package org.radarbase.user.model

import kotlinx.serialization.Serializable

@Serializable
data class KratosRecoveryFlow(
    val id: String,
)

@Serializable
data class KratosRecoverySubmitRequest(
    val email: String,
    val method: String = "link",
)
