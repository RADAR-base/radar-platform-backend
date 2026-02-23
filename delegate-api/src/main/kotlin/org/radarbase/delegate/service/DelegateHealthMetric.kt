package org.radarbase.delegate.service

import jakarta.inject.Singleton
import org.radarbase.jersey.service.HealthService

@Singleton
class DelegateHealthMetric(
    name: String = "delegate-api",
) : HealthService.Metric(name) {
    val healthy: Boolean = true
    val message: String? = null

    override suspend fun computeMetrics(): Map<String, Any> {
        // TODO: get metrics from other services
        return mapOf("status" to "ok")
    }

    override suspend fun computeStatus(): HealthService.Status? {
        // TODO: Check if the all the other services are healthy
        return HealthService.Status.UP
    }
}
