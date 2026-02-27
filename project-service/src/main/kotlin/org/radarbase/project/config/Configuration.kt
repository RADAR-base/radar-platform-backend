package org.radarbase.project.config

import jakarta.inject.Singleton
import org.radarbase.core.config.LoggingConfig

@Singleton
interface BaseConfiguration {
    val radar: RadarConfiguration
    val server: ServerConfiguration
    val logging: LoggingConfig
}

data class RadarConfiguration(
    val baseUrl: String,
    val timeout: Long,
    val maxRetries: Int,
    val auth: RadarAuthConfiguration,
) {
    data class RadarAuthConfiguration(
        val enabled: Boolean,
        val tokenHeader: String,
        val tokenPrefix: String,
    )
}

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