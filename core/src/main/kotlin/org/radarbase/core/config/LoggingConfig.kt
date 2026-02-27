package org.radarbase.core.config

/**
 * Common interface for logging configuration shared across services.
 * Implement in each service's config class to enable the shared
 * [org.radarbase.core.logging.LoggingConfigurator].
 */
data class LoggingConfig(
    val level: String = "INFO",
    val file: String = "logs/platform-backend-core.log",
    val pattern: String = "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n",
)
