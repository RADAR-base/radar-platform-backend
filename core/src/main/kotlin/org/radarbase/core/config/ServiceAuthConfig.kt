package org.radarbase.core.config

/**
 * Service-to-service OAuth2 client-credentials configuration.
 * TODO: Replace this with radar commons support classes
 */
data class ServiceAuthConfig(
    val clientId: String = "",
    val clientSecret: String = "",
    val tokenEndpoint: String = "",
    val scope: String? = null,
    val audience: String? = null,
)
