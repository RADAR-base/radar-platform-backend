package org.radarbase.gateway.service

import jakarta.inject.Inject
import org.radarbase.gateway.client.RadarGatewayClient
import org.radarbase.jersey.service.HealthService
import org.slf4j.LoggerFactory

class GatewayHealthMetric(
    @Inject private val radarGatewayClient: RadarGatewayClient,
    name: String = "gateway-service",
) : HealthService.Metric(name) {
    private val logger = LoggerFactory.getLogger(GatewayHealthMetric::class.java)

    override suspend fun computeMetrics(): Map<String, Any> {
        val gatewayStatus = try {
            if (radarGatewayClient.checkHealth()) {
                "connected"
            } else {
                "disconnected"
            }
        } catch (e: Exception) {
            logger.error("RADAR-Gateway health check failed", e)
            "disconnected"
        }

        return mapOf(
            "status" to "ok",
            "gateway" to gatewayStatus,
            "version" to "1.0.0",
        )
    }

    override suspend fun computeStatus(): HealthService.Status {
        val metrics = computeMetrics()

        return when {
            metrics["gateway"] == "disconnected" -> {
                logger.warn(
                    "Service dependencies are not healthy: gateway=${metrics["gateway"]}",
                )
                HealthService.Status.DOWN
            }

            else -> {
                HealthService.Status.UP
            }
        }
    }
}
