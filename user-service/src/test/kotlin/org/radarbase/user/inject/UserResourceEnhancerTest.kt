package org.radarbase.user.inject

import io.mockk.mockk
import io.mockk.verify
import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.radarbase.jersey.filter.Filters
import org.radarbase.user.client.KratosClient
import org.radarbase.user.client.KtorClientFactory
import org.radarbase.user.client.RadarClient
import org.radarbase.user.config.UserServiceConfig
import org.radarbase.user.service.UserService
import org.radarbase.user.service.UserServiceImpl

class UserResourceEnhancerTest {
    private val config = mockk<UserServiceConfig>()
    private val enhancer = UserResourceEnhancer(config)
    private val binder = mockk<AbstractBinder>(relaxed = true)

    @Test
    fun `test packages contain expected values`() {
        val packages = enhancer.packages
        assertTrue(packages.contains("org.radarbase.user.resource"))
        assertTrue(packages.contains("org.radarbase.user.filter"))
    }

    @Test
    fun `test classes contain expected values`() {
        val classes = enhancer.classes
        assertTrue(classes.contains(Filters.logResponse))
    }

    @Test
    fun `test enhance binds configuration`() {
        enhancer.enhanceBinder(binder)
        verify { binder.bind(config).to(UserServiceConfig::class.java).`in`(Singleton::class.java) }
    }

    @Test
    fun `test enhance binds services`() {
        enhancer.enhanceBinder(binder)
        verify { binder.bind(UserServiceImpl::class.java).to(UserService::class.java).`in`(Singleton::class.java) }
    }

    @Test
    fun `test enhance binds clients`() {
        enhancer.enhanceBinder(binder)

        verify { binder.bind(KratosClient::class.java).to(KratosClient::class.java).`in`(Singleton::class.java) }
        verify { binder.bind(RadarClient::class.java).to(RadarClient::class.java).`in`(Singleton::class.java) }
        verify {
            binder
                .bind(
                    KtorClientFactory::class.java,
                ).to(KtorClientFactory::class.java)
                .`in`(Singleton::class.java)
        }
    }

    @Test
    fun `test enhance binds health metrics`() {
        enhancer.enhanceBinder(binder)
        verify {
            binder
                .bind(
                    any(),
                ).named(
                    "user-service",
                ).to(org.radarbase.jersey.service.HealthService.Metric::class.java)
                .`in`(Singleton::class.java)
        }
    }
}
