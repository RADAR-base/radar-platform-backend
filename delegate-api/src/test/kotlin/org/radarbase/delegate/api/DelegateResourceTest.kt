package org.radarbase.delegate.api

import io.mockk.coEvery
import io.mockk.mockk
import jakarta.ws.rs.core.Application
import jakarta.ws.rs.core.Response
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.test.JerseyTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.delegate.service.DelegateApiService
import org.radarbase.delegate.service.DelegateHealthMetric
import org.radarbase.jersey.service.HealthService

class DelegateResourceTest : JerseyTest() {
    private lateinit var apiService: DelegateApiService
    private lateinit var healthMetric: DelegateHealthMetric

    override fun configure(): Application {
        apiService = mockk(relaxed = true)
        healthMetric = mockk(relaxed = true)

        coEvery { healthMetric.computeMetrics() } returns mapOf<String, Any>("status" to "ok")
        coEvery { healthMetric.computeStatus() } returns HealthService.Status.UP

        return ResourceConfig(DelegateResource::class.java)
            .register(
                object : AbstractBinder() {
                    override fun configure() {
                        bind(apiService).to(DelegateApiService::class.java)
                        bind(healthMetric).to(DelegateHealthMetric::class.java)
                    }
                },
            )
    }

    @BeforeEach
    override fun setUp() {
        super.setUp()
    }

    @AfterEach
    override fun tearDown() {
        super.tearDown()
    }

    @Test
    fun testHealthCheck() {
        val response =
            target("/api/v2/delegate/health")
                .request()
                .get()

        assertEquals(Response.Status.OK.statusCode, response.status)
    }
}
