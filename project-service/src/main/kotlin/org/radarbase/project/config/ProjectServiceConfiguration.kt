package org.radarbase.project.config

import jakarta.inject.Singleton
import org.radarbase.core.config.LoggingConfig
import org.radarbase.core.config.ServiceAuthConfig
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.project.inject.ProjectJwtEnhancerFactory

@Singleton
data class ProjectServiceConfiguration(
    val radar: RadarConfiguration,
    val server: ServerConfiguration,
    val logging: LoggingConfig,
    val serviceAuth: ServiceAuthConfig = ServiceAuthConfig(),
) {
    val resourceConfig: Class<out EnhancerFactory> = ProjectJwtEnhancerFactory::class.java

    fun validate() {
        // Add validation logic if needed
    }
}
