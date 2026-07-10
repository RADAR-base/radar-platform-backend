package org.radarbase.gateway.config

/**
 * Connection settings for the upstream RADAR-Gateway that this service proxies.
 */
data class GatewayConfig(
    val baseUrl: String = "http://radar-gateway:8090/radar-gateway",
    val timeout: Long = 30,
    val maxRetries: Int = 3,
)
