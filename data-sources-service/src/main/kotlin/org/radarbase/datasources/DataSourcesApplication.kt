package org.radarbase.datasources

import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.core.logging.LoggingConfigurator
import org.radarbase.core.util.ServiceTokenProvider
import org.radarbase.datasources.client.RadarSourcesClient
import org.radarbase.datasources.client.RestSourcesClient
import org.radarbase.datasources.config.DataSourcesServiceConfig
import org.radarbase.datasources.service.DataSourcesService
import org.radarbase.datasources.service.DataSourcesServiceImpl
import org.radarbase.jersey.config.ConfigLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import kotlin.system.exitProcess

class DataSourcesApplication {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger("org.radarbase.datasources.DataSourcesApplication")

        @JvmStatic
        fun main(args: Array<String>) {
            val config =
                try {
                    ConfigLoader.loadConfig<DataSourcesServiceConfig>("config.yaml", args)
                } catch (_: IllegalArgumentException) {
                    logger.error("No configuration file was found.")
                    logger.error("Usage: data-sources-service <config-file>")
                    exitProcess(1)
                }

            try {
                config.validate()
            } catch (ex: IllegalStateException) {
                logger.error("Configuration incomplete: {}", ex.message)
                exitProcess(1)
            }

            LoggingConfigurator(config.logging).apply(LoggingConfigurator::configure)

            val resourceConfig =
                ResourceConfig()
                    .packages(
                        "org.radarbase.datasources.api",
                        "org.radarbase.datasources.service",
                        "org.radarbase.datasources.config",
                    ).register(
                        object : AbstractBinder() {
                            override fun configure() {
                                bind(config).to(DataSourcesServiceConfig::class.java)
                                bind(ServiceTokenProvider(config.serviceAuth)).to(ServiceTokenProvider::class.java)
                                bind(RadarSourcesClient::class.java).to(RadarSourcesClient::class.java)
                                bind(RestSourcesClient::class.java).to(RestSourcesClient::class.java)
                                bind(DataSourcesServiceImpl::class.java).to(DataSourcesService::class.java)
                            }
                        },
                    )

            val server: HttpServer =
                GrizzlyHttpServerFactory
                    .createHttpServer(URI.create(config.server.baseUri), resourceConfig)

            Runtime.getRuntime().addShutdownHook(
                Thread {
                    server.shutdownNow()
                },
            )

            logger.info("Starting data-sources-service at ${config.server.baseUri}")
            server.start()
        }
    }
}
