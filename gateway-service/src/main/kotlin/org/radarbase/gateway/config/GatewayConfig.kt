package org.radarbase.gateway.config

/**
 * Connection settings for the upstream RADAR-Gateway that this service proxies.
 *
 * @param baseUrl root URL of the gateway (e.g. `http://radar-gateway:8090/radar-gateway`).
 * @param timeout request/connect timeout in seconds, applied to the Ktor HTTP client.
 * @param maxRetries number of retries on connection/timeout failures.
 */
data class GatewayConfig(
    val baseUrl: String = "http://radar-gateway:8090/radar-gateway",
    val timeout: Long = 30,
    val maxRetries: Int = 3,
)
