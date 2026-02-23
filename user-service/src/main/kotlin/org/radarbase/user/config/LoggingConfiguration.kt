package org.radarbase.user.config

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.FileAppender
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class LoggingConfiguration
    @Inject
    constructor(
        private val config: UserServiceConfig,
    ) {
        fun configure() {
            val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
            val rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)

            // Set log level
            rootLogger.level = Level.toLevel(config.logging.level)

            // Configure file appender
            val fileAppender =
                FileAppender<ILoggingEvent>().apply {
                    context = loggerContext
                    name = "FILE"
                    file = config.logging.file
                    encoder =
                        PatternLayoutEncoder().apply {
                            context = loggerContext
                            pattern = config.logging.pattern
                            start()
                        }
                    start()
                }

            // Add file appender to root logger
            rootLogger.addAppender(fileAppender)

            // Log configuration
            rootLogger.info("Logging configured with level: ${config.logging.level}, file: ${config.logging.file}")
        }
    }
