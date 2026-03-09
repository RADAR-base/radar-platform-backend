package org.radarbase.project.config

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

