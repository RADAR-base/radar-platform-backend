package org.radarbase.gateway.inject

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.glassfish.jersey.process.internal.RequestScoped
import org.radarbase.core.filter.AuthorizationFilter
import org.radarbase.core.util.KtorClientFactory
import org.radarbase.core.util.ServiceTokenProvider
import org.radarbase.gateway.client.RadarGatewayClient
import org.radarbase.gateway.config.GatewayServiceConfiguration
import org.radarbase.gateway.service.GatewayHealthMetric
import org.radarbase.gateway.service.GatewayService
import org.radarbase.gateway.service.GatewayServiceImpl
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.filter.Filters
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.jersey.service.HealthService.Metric
import org.radarbase.jersey.service.ScopedAsyncCoroutineService

class GatewayResourceEnhancer(
    private val config: GatewayServiceConfiguration,
) : JerseyResourceEnhancer {
    private val serviceTokenProvider: ServiceTokenProvider = ServiceTokenProvider(config.serviceAuth)

    override val packages: Array<String> = arrayOf(
        "org.radarbase.gateway.resource",
        "org.radarbase.gateway.filter",
    )

    override val classes: Array<Class<*>> = arrayOf(
        Filters.logResponse,
        MultiPartFeature::class.java,
        AuthorizationFilter::class.java,
    )

    override fun AbstractBinder.enhance() {
        // Bind configuration
        bind(config)
            .to(GatewayServiceConfiguration::class.java)
            .`in`(Singleton::class.java)

        // Bind services
        bind(GatewayServiceImpl::class.java)
            .to(GatewayService::class.java)
            .`in`(Singleton::class.java)

        // Bind clients
        bind(RadarGatewayClient::class.java)
            .to(RadarGatewayClient::class.java)
            .`in`(Singleton::class.java)

        bind(KtorClientFactory::class.java)
            .to(KtorClientFactory::class.java)
            .`in`(Singleton::class.java)

        bind(GatewayHealthMetric::class.java)
            .named("gateway-service")
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
