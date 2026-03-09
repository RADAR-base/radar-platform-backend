package org.radarbase.user

import org.radarbase.core.logging.LoggingConfigurator
import org.radarbase.jersey.GrizzlyServer
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.user.config.UserServiceConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import kotlin.system.exitProcess

class UserApplication {
    companion object {
        val logger: Logger = LoggerFactory.getLogger("org.radarbase.gateway.MainKt")

        @JvmStatic
        fun main(args: Array<String>) {
            val config = try {
                    ConfigLoader.loadConfig<UserServiceConfig>("config.yaml", args)
                } catch (_: IllegalArgumentException) {
                    logger.error("No configuration file was found.")
                    logger.error("Usage: user-service <config-file>")
                    exitProcess(1)
                }

            try {
                config.validate()
            } catch (ex: IllegalStateException) {
                logger.error("Configuration incomplete: {}", ex.message)
                exitProcess(1)
            }

            // Configure logging
            LoggingConfigurator(config.logging).apply(LoggingConfigurator::configure)

            val resources = ConfigLoader.loadResources(config.resourceConfig, config)

            val server = GrizzlyServer(URI(config.server.baseUri), resources, config.server.isJmxEnabled)
            server.listen()

            Runtime.getRuntime().addShutdownHook(
                Thread {
                    server.shutdown()
                },
            )

            println("Starting user-service at ${config.server.baseUri}")
            server.start()
        }
    }
}
