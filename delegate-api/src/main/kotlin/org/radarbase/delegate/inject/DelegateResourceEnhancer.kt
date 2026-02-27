package org.radarbase.delegate.inject

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.delegate.config.DelegateConfig
import org.radarbase.delegate.service.ConfigService
import org.radarbase.delegate.service.ConfigServiceImpl
import org.radarbase.delegate.service.DataSourcesService
import org.radarbase.delegate.service.DataSourcesServiceImpl
import org.radarbase.delegate.service.DelegateApiService
import org.radarbase.delegate.service.DelegateHealthMetric
import org.radarbase.delegate.service.HttpClientService
import org.radarbase.delegate.service.ParticipantService
import org.radarbase.delegate.service.ParticipantServiceImpl
import org.radarbase.delegate.service.UserService
import org.radarbase.delegate.service.UserServiceImpl
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.filter.Filters
import org.radarbase.jersey.service.HealthService
import org.radarbase.jersey.service.ProjectService
import org.radarbase.delegate.service.ProjectService as DelegateProjectService
import org.radarbase.delegate.service.ProjectServiceImpl as DelegateProjectServiceImpl

class DelegateResourceEnhancer(
    private val config: DelegateConfig,
) : JerseyResourceEnhancer {
    override val packages: Array<String> = arrayOf(
        "org.radarbase.delegate.filter",
        "org.radarbase.delegate.resource",
        "org.radarbase.delegate.api",
        "org.radarbase.delegate.service",
        "org.radarbase.delegate.config",
    )

    override val classes: Array<Class<*>> = arrayOf(
        Filters.logResponse,
    )

    override fun AbstractBinder.enhance() {
        bind(config)
            .to(DelegateConfig::class.java)

        bind(UnverifiedProjectService::class.java)
            .to(ProjectService::class.java)
            .`in`(Singleton::class.java)

        bind(DelegateProjectServiceImpl::class.java)
            .to(DelegateProjectService::class.java)
            .`in`(Singleton::class.java)

        bind(UserServiceImpl::class.java)
            .to(UserService::class.java)
            .`in`(Singleton::class.java)

        bind(ParticipantServiceImpl::class.java)
            .to(ParticipantService::class.java)
            .`in`(Singleton::class.java)

        bind(ConfigServiceImpl::class.java)
            .to(ConfigService::class.java)
            .`in`(Singleton::class.java)

        bind(DataSourcesServiceImpl::class.java)
            .to(DataSourcesService::class.java)
            .`in`(Singleton::class.java)

        bind(DelegateHealthMetric::class.java)
            .to(HealthService.Metric::class.java)
            .`in`(Singleton::class.java)

        bind(DelegateHealthMetric::class.java)
            .to(DelegateHealthMetric::class.java)
            .`in`(Singleton::class.java)

        bind(DelegateApiService::class.java)
            .to(DelegateApiService::class.java)
            .`in`(Singleton::class.java)

        bind(HttpClientService::class.java)
            .to(HttpClientService::class.java)
            .`in`(Singleton::class.java)
    }

    /** Project service without validation of the project's existence. */
    class UnverifiedProjectService : ProjectService {
        override suspend fun ensureProject(projectId: String) {
        }

        override suspend fun ensureOrganization(organizationId: String) {
        }

        override suspend fun ensureSubject(
            projectId: String,
            userId: String,
        ) {
        }

        override suspend fun listProjects(organizationId: String): List<String> = emptyList()

        override suspend fun projectOrganization(projectId: String): String = "default"
    }
}
