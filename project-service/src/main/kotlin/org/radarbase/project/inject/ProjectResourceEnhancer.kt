package org.radarbase.project.inject

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.process.internal.RequestScoped
import org.radarbase.core.filter.AuthorizationFilter
import org.radarbase.core.util.KtorClientFactory
import org.radarbase.core.util.ServiceTokenProvider
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.filter.Filters
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.jersey.service.HealthService.Metric
import org.radarbase.jersey.service.ScopedAsyncCoroutineService
import org.radarbase.project.client.RadarProjectClient
import org.radarbase.project.config.ProjectServiceConfiguration
import org.radarbase.project.service.ProjectHealthMetric
import org.radarbase.project.service.ProjectService
import org.radarbase.project.service.ProjectServiceImpl

class ProjectResourceEnhancer(
    private val config: ProjectServiceConfiguration,
) : JerseyResourceEnhancer {
    private val serviceTokenProvider: ServiceTokenProvider = ServiceTokenProvider(config.serviceAuth)

    override val packages: Array<String> =
        arrayOf(
            "org.radarbase.project.resource",
            "org.radarbase.project.filter",
        )

    override val classes: Array<Class<*>> =
        arrayOf(
            Filters.logResponse,
            AuthorizationFilter::class.java,
        )

    override fun AbstractBinder.enhance() {
        // Bind configuration
        bind(config)
            .to(ProjectServiceConfiguration::class.java)
            .`in`(Singleton::class.java)

        // Bind services
        bind(ProjectServiceImpl::class.java)
            .to(ProjectService::class.java)
            .`in`(Singleton::class.java)

        // Bind clients
        bind(RadarProjectClient::class.java)
            .to(RadarProjectClient::class.java)
            .`in`(Singleton::class.java)

        bind(KtorClientFactory::class.java)
            .to(KtorClientFactory::class.java)
            .`in`(Singleton::class.java)

        bind(ProjectHealthMetric::class.java)
            .named("project-service")
            .to(Metric::class.java)
            .`in`(Singleton::class.java)

        bind(ScopedAsyncCoroutineService::class.java)
            .to(AsyncCoroutineService::class.java)
            .`in`(RequestScoped::class.java)

        bind(serviceTokenProvider::class.java)
            .to(ServiceTokenProvider::class.java)
            .`in`(Singleton::class.java)
    }
}
