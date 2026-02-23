package org.radarbase.project.config

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class ProjectLoggingConfiguration
    @Inject
    constructor(
        private val config: ProjectServiceConfiguration,
    ) {
        fun configure() {
            val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
            val rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)

            // Set log level
            rootLogger.level = Level.toLevel(config.logging.level)

            // Log configuration
            rootLogger.info(
                "Logging configured with level: ${config.logging.level}, format: ${config.logging.format}",
            )
        }
    }
