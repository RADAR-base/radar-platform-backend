package org.radarbase.gateway.config

import jakarta.inject.Singleton
import org.radarbase.core.config.LoggingConfig
import org.radarbase.core.config.ServiceAuthConfig
import org.radarbase.gateway.inject.GatewayJwtEnhancerFactory
import org.radarbase.jersey.enhancer.EnhancerFactory

@Singleton
data class GatewayServiceConfiguration(
    val gateway: GatewayConfiguration,
    val server: ServerConfiguration,
    val logging: LoggingConfig,
    val serviceAuth: ServiceAuthConfig = ServiceAuthConfig(),
) {
    val resourceConfig: Class<out EnhancerFactory> = GatewayJwtEnhancerFactory::class.java

    fun validate() {
        // Add validation logic if needed
    }
}
