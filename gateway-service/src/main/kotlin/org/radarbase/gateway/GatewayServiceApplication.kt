package org.radarbase.gateway

import org.radarbase.core.logging.LoggingConfigurator
import org.radarbase.gateway.config.GatewayServiceConfiguration
import org.radarbase.jersey.GrizzlyServer
import org.radarbase.jersey.config.ConfigLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class GatewayServiceApplication {
    companion object {
        val logger: Logger = LoggerFactory.getLogger("org.radarbase.gateway.GatewayServiceApplication")

        @JvmStatic
        fun main(args: Array<String>) {
            val config = try {
                ConfigLoader.loadConfig<GatewayServiceConfiguration>(
                    listOf("gateway-service/src/main/resources/config.yaml", "/etc/gateway-service/config.yaml"),
                    args,
                )
            } catch (_: IllegalArgumentException) {
                logger.error("No configuration file was found.")
                logger.error("Usage: gateway-service <config-file>")
                exitProcess(1)
            }

            try {
                config.validate()
            } catch (ex: IllegalStateException) {
                logger.error("Configuration incomplete: {}", ex.message)
                exitProcess(1)
            }

            LoggingConfigurator(config.logging).apply(LoggingConfigurator::configure)

            val resources = ConfigLoader.loadResources(config.resourceConfig, config)

            val server = GrizzlyServer(config.server.baseUri, resources, config.server.isJmxEnabled)
            server.listen()

            Runtime.getRuntime().addShutdownHook(
                Thread {
                    server.shutdown()
                },
            )

            println("Starting gateway-service at ${config.server.baseUri}")
            server.start()
        }
    }
}
