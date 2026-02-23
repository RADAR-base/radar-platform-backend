package org.radarbase.config.inject

import org.radarbase.config.config.ConfigServiceConfig
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer

class ConfigJwtEnhancerFactory(
    private val config: ConfigServiceConfig,
) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> =
        listOf(
            ConfigResourceEnhancer(config),
            Enhancers.exception,
            Enhancers.health,
        )
}
