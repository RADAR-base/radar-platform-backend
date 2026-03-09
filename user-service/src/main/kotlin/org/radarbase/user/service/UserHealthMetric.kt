package org.radarbase.user.service

import jakarta.inject.Inject
import org.radarbase.jersey.service.HealthService
import org.radarbase.user.client.KratosClient
import org.radarbase.user.client.RadarClient
import org.slf4j.LoggerFactory

class UserHealthMetric(
    @Inject private val kratosClient: KratosClient,
    @Inject private val radarClient: RadarClient,
    name: String = "user-service",
) : HealthService.Metric(name) {
    private val logger = LoggerFactory.getLogger(UserHealthMetric::class.java)

    override suspend fun computeMetrics(): Map<String, Any> {
        val kratosStatus =
            try {
                if (kratosClient.checkHealth()) {
                    "connected"
                } else {
                    "disconnected"
                }
            } catch (e: Exception) {
                logger.error("Kratos health check failed", e)
                "disconnected"
            }

        val radarStatus =
            try {
                if (radarClient.checkHealth()) {
                    "connected"
                } else {
                    "disconnected"
                }
            } catch (e: Exception) {
                logger.error("Radar health check failed", e)
                "disconnected"
            }

        return mapOf(
            "status" to "ok",
            "kratos" to kratosStatus,
            "radar" to radarStatus,
            "version" to "1.0.0",
        )
    }

    override suspend fun computeStatus(): HealthService.Status {
        val metrics = computeMetrics()

        return when {
            metrics["kratos"] == "disconnected" || metrics["radar"] == "disconnected" -> {
                logger.warn(
                    "Service dependencies are not healthy: kratos=${metrics["kratos"]}, radar=${metrics["radar"]}",
                )
                HealthService.Status.DOWN
            }

            else -> {
                HealthService.Status.UP
            }
        }
    }
}
