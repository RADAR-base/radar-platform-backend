package org.radarbase.user.model

import kotlinx.serialization.Serializable

@Serializable
data class ManagementPortalProxyResponse(
    val status: Int,
    val body: String? = null,
)
