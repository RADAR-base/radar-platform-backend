package org.radarbase.delegate

import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.delegate.config.DelegateConfig
import org.radarbase.jersey.config.ConfigLoader
import java.net.URI

class DelegateApplication {
    companion object {
        private const val BASE_URI = "http://0.0.0.0:8080/"

        @JvmStatic
        fun main(args: Array<String>) {
            val config = ConfigLoader.loadConfig<DelegateConfig>(
                listOf("delegate-api/src/main/resources/config.yaml", "/etc/delegate-api/config.yaml"),
                args,
            )

            val resourceConfig: ResourceConfig = ConfigLoader.loadResources(config.resourceConfig, config)

            val server: HttpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), resourceConfig)

            Runtime.getRuntime().addShutdownHook(
                Thread {
                    server.shutdownNow()
                },
            )

            server.start()
        }
    }
}
