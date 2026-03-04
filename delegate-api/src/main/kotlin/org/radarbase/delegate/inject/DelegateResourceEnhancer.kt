package org.radarbase.delegate.inject

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.contract.client.ClientsContract
import org.radarbase.contract.utils.Env.CONFIG_SERVICE
import org.radarbase.contract.utils.Env.DATA_SOURCES_SERVICE
import org.radarbase.contract.utils.Env.PARTICIPANT_SERVICE
import org.radarbase.contract.utils.Env.PROJECT_SERVICE
import org.radarbase.contract.utils.Env.USER_SERVICE
import org.radarbase.delegate.config.DelegateConfig
import org.radarbase.delegate.service.DelegateApiService
import org.radarbase.delegate.service.DelegateHealthMetric
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.filter.Filters
import org.radarbase.jersey.service.HealthService
import org.radarbase.jersey.service.ProjectService

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
        // Register contract clients for all downstream services
        config.contract.let { c ->
            ClientsContract.registerService(USER_SERVICE, c.user)
            ClientsContract.registerService(PARTICIPANT_SERVICE, c.participant)
            ClientsContract.registerService(PROJECT_SERVICE, c.project)
            ClientsContract.registerService(CONFIG_SERVICE, c.config)
            ClientsContract.registerService(DATA_SOURCES_SERVICE, c.dataSources)
        }

        bind(config)
            .to(DelegateConfig::class.java)

        bind(UnverifiedProjectService::class.java)
            .to(ProjectService::class.java)
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
