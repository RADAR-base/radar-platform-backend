package org.radarbase.gateway.config

import jakarta.inject.Singleton
import org.radarbase.core.config.LoggingConfig
import org.radarbase.core.config.ServiceAuthConfig
import org.radarbase.gateway.inject.GatewayJwtEnhancerFactory
import org.radarbase.jersey.enhancer.EnhancerFactory

@Singleton
data class GatewayServiceConfiguration(
    val gateway: GatewayConfig = GatewayConfig(),
    val server: ServerConfiguration = ServerConfiguration(),
    val logging: LoggingConfig = LoggingConfig(),
    val serviceAuth: ServiceAuthConfig = ServiceAuthConfig(),
) {
    val resourceConfig: Class<out EnhancerFactory> = GatewayJwtEnhancerFactory::class.java

    fun validate() {
        check(gateway.baseUrl.isNotBlank()) { "gateway.baseUrl must not be blank" }
    }
}
