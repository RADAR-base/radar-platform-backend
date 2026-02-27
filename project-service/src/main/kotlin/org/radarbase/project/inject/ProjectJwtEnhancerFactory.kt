package org.radarbase.project.inject

import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.project.config.ProjectServiceConfiguration

class ProjectJwtEnhancerFactory(
    private val config: ProjectServiceConfiguration,
) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> = listOf(
        ProjectResourceEnhancer(config),
        Enhancers.ecdsa,
        Enhancers.exception,
        Enhancers.health,
    )
}
