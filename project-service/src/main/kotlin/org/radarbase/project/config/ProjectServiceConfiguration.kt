package org.radarbase.project.config

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.project.inject.ProjectJwtEnhancerFactory

@Singleton
data class ProjectServiceConfiguration
    @Inject
    constructor(
        override val radar: RadarConfiguration,
        override val server: ServerConfiguration,
        override val logging: LoggingConfiguration,
        val serviceAuth: ServiceAuthConfig = ServiceAuthConfig(),
    ) : BaseConfiguration {
        val resourceConfig: Class<out EnhancerFactory> = ProjectJwtEnhancerFactory::class.java

        fun validate() {
            // Add validation logic if needed
        }

        data class ServiceAuthConfig(
            val clientId: String = "",
            val clientSecret: String = "",
            val tokenEndpoint: String = "",
            val scope: String? = null,
        )
    }
