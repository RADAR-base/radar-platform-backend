package org.radarbase.core.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.FileAppender
import org.radarbase.core.config.LoggingConfig
import org.slf4j.LoggerFactory

/**
 * Shared logging configurator that sets the root log level and
 * optionally adds a file appender based on a [org.radarbase.core.config.LoggingConfig].
 */
class LoggingConfigurator(
    private val config: LoggingConfig,
) {
    fun configure() {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        val rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)

        rootLogger.level = Level.toLevel(config.level)

        val fileAppender =
            FileAppender<ILoggingEvent>().apply {
                context = loggerContext
                name = "FILE"
                file = config.file
                encoder =
                    PatternLayoutEncoder().apply {
                        context = loggerContext
                        pattern = config.pattern
                        start()
                    }
                start()
            }

        rootLogger.addAppender(fileAppender)
        rootLogger.info("Logging configured with level: ${config.level}, file: ${config.file}")
    }
}
