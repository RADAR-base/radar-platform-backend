package org.radarbase.delegate.config

import org.radarbase.delegate.inject.DelegateJwtEnhancerFactory
import org.radarbase.jersey.enhancer.EnhancerFactory

data class DelegateConfig(
    val auth: AuthConfig,
    val userService: UserServiceConfig,
    val participantService: ParticipantServiceConfig,
    val projectService: ProjectServiceConfig,
    val configService: ConfigServiceConfig,
    val dataSourcesService: DataSourcesServiceConfig,
) {
    val resourceConfig: Class<out EnhancerFactory> = DelegateJwtEnhancerFactory::class.java
}
