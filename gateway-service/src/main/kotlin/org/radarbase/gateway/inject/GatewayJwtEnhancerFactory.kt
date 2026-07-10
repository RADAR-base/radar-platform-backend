package org.radarbase.gateway.inject

import org.radarbase.gateway.config.GatewayServiceConfiguration
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer

class GatewayJwtEnhancerFactory(
    private val config: GatewayServiceConfiguration,
) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> = listOf(
        GatewayResourceEnhancer(config),
        Enhancers.ecdsa,
        Enhancers.exception,
        Enhancers.health,
    )
}
