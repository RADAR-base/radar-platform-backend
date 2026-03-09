package org.radarbase.user.inject

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
import org.radarbase.user.client.KratosClient
import org.radarbase.user.client.RadarClient
import org.radarbase.user.config.UserServiceConfig
import org.radarbase.user.service.UserHealthMetric
import org.radarbase.user.service.UserService
import org.radarbase.user.service.UserServiceImpl

class UserResourceEnhancer(
    private val config: UserServiceConfig,
) : JerseyResourceEnhancer {
    private val serviceTokenProvider: ServiceTokenProvider = ServiceTokenProvider(config.serviceAuth)

    override val packages: Array<String> =
        arrayOf(
            "org.radarbase.user.resource",
            "org.radarbase.user.filter",
        )

    override val classes: Array<Class<*>> =
        arrayOf(
            Filters.logResponse,
            AuthorizationFilter::class.java,
        )

    override fun AbstractBinder.enhance() {
        // Bind configuration
        bind(config)
            .to(UserServiceConfig::class.java)
            .`in`(Singleton::class.java)

        // Bind services
        bind(UserServiceImpl::class.java)
            .to(UserService::class.java)
            .`in`(Singleton::class.java)

        // Bind clients
        bind(KratosClient::class.java)
            .to(KratosClient::class.java)
            .`in`(Singleton::class.java)

        bind(RadarClient::class.java)
            .to(RadarClient::class.java)
            .`in`(Singleton::class.java)

        bind(KtorClientFactory::class.java)
            .to(KtorClientFactory::class.java)
            .`in`(Singleton::class.java)

        bind(UserHealthMetric::class.java)
            .named("user-service")
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
