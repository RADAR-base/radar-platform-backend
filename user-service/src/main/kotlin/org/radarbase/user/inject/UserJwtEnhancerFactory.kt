package org.radarbase.user.inject

import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.user.config.UserServiceConfig

class UserJwtEnhancerFactory(
    private val config: UserServiceConfig,
) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> =
        listOf(
            UserResourceEnhancer(config),
            Enhancers.ecdsa,
            Enhancers.exception,
            Enhancers.health,
        )
}
