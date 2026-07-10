package org.radarbase.gateway.config

data class ServerConfiguration(
    val port: Int,
    val host: String,
    val cors: CorsConfiguration,
    val baseUri: String = "http://$host:$port",
    val isJmxEnabled: Boolean = false,
) {
    data class CorsConfiguration(
        val enabled: Boolean,
        val allowedOrigins: List<String>,
        val allowedMethods: List<String>,
        val allowedHeaders: List<String>,
    )
}
