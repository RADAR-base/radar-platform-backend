package org.radarbase.project.service

import jakarta.inject.Inject
import org.radarbase.jersey.service.HealthService
import org.radarbase.project.client.RadarProjectClient
import org.slf4j.LoggerFactory

class ProjectHealthMetric(
    @Inject private val radarProjectClient: RadarProjectClient,
    name: String = "project-service",
) : HealthService.Metric(name) {
    private val logger = LoggerFactory.getLogger(ProjectHealthMetric::class.java)

    override suspend fun computeMetrics(): Map<String, Any> {
        val radarStatus =
            try {
                if (radarProjectClient.checkHealth()) {
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
            "radar" to radarStatus,
            "version" to "1.0.0",
        )
    }

    override suspend fun computeStatus(): HealthService.Status {
        val metrics = computeMetrics()

        return when {
            metrics["radar"] == "disconnected" -> {
                logger.warn(
                    "Service dependencies are not healthy: radar=${metrics["radar"]}",
                )
                HealthService.Status.DOWN
            }

            else -> {
                HealthService.Status.UP
            }
        }
    }
}
