package org.radarbase.gateway.config

/**
 * Connection settings for the upstream RADAR-Gateway.
 *
 * @param baseUrl root URL of the gateway (e.g. `http://radar-gateway:8090/radar-gateway`).
 * @param timeout request/connect timeout, in seconds.
 * @param maxRetries number of retries on connection/timeout failures.
 */
data class GatewayConfiguration(
    val baseUrl: String,
    val timeout: Long,
    val maxRetries: Int,
    val auth: GatewayAuthConfiguration,
) {
    data class GatewayAuthConfiguration(
        val enabled: Boolean,
        val tokenHeader: String,
        val tokenPrefix: String,
    )
}
