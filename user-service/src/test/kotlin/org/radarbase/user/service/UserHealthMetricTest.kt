package org.radarbase.user.service

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.jersey.service.HealthService
import org.radarbase.user.client.KratosClient
import org.radarbase.user.client.RadarClient
import org.radarbase.user.config.UserServiceConfig

class UserHealthMetricTest {
    private lateinit var healthMetric: UserHealthMetric
    private lateinit var kratosClient: KratosClient
    private lateinit var radarClient: RadarClient
    private lateinit var config: UserServiceConfig

    @BeforeEach
    fun setup() {
        kratosClient = mockk()
        radarClient = mockk()
        config = mockk()
        healthMetric = UserHealthMetric(kratosClient, radarClient)
    }

    @Test
    fun `test health metric name`() {
        assertEquals("user-service", healthMetric.name)
    }

    @Test
    fun `test compute metrics when all services are healthy`() =
        runBlocking {
            // Given
            coEvery { kratosClient.checkHealth() } returns true
            coEvery { radarClient.checkHealth() } returns true

            // When
            val metrics = healthMetric.computeMetrics()

            // Then
            assertEquals("ok", metrics["status"])
            assertEquals("connected", metrics["kratos"])
            assertEquals("connected", metrics["radar"])
            assertEquals("1.0.0", metrics["version"])
        }

    @Test
    fun `test compute metrics when Kratos is down`() =
        runBlocking {
            // Given
            coEvery { kratosClient.checkHealth() } returns false
            coEvery { radarClient.checkHealth() } returns true

            // When
            val metrics = healthMetric.computeMetrics()

            // Then
            assertEquals("ok", metrics["status"])
            assertEquals("disconnected", metrics["kratos"])
            assertEquals("connected", metrics["radar"])
        }

    @Test
    fun `test compute metrics when Radar is down`() =
        runBlocking {
            // Given
            coEvery { kratosClient.checkHealth() } returns true
            coEvery { radarClient.checkHealth() } returns false

            // When
            val metrics = healthMetric.computeMetrics()

            // Then
            assertEquals("ok", metrics["status"])
            assertEquals("connected", metrics["kratos"])
            assertEquals("disconnected", metrics["radar"])
        }

    @Test
    fun `test compute status returns UP when all services are healthy`() =
        runBlocking {
            // Given
            coEvery { kratosClient.checkHealth() } returns true
            coEvery { radarClient.checkHealth() } returns true

            // When
            val status = healthMetric.computeStatus()

            // Then
            assertEquals(HealthService.Status.UP, status)
        }

    @Test
    fun `test compute status returns DOWN when any service is unhealthy`() =
        runBlocking {
            // Given
            coEvery { kratosClient.checkHealth() } returns false
            coEvery { radarClient.checkHealth() } returns true

            // When
            val status = healthMetric.computeStatus()

            // Then
            assertEquals(HealthService.Status.DOWN, status)
        }

    @Test
    fun `test compute metrics handles exceptions gracefully`() =
        runBlocking {
            // Given
            coEvery { kratosClient.checkHealth() } throws RuntimeException("Connection failed")
            coEvery { radarClient.checkHealth() } returns true

            // When
            val metrics = healthMetric.computeMetrics()

            // Then
            assertEquals("ok", metrics["status"])
            assertEquals("disconnected", metrics["kratos"])
            assertEquals("connected", metrics["radar"])
        }
}
