package org.radarbase.gateway.config

import java.net.URI

/**
 * HTTP server settings. CORS and other cross-cutting concerns are handled by radar-jersey, so
 * only the values this service actually needs are declared here.
 *
 * @param baseUri base URL to serve on; determines the bind host, port and base path.
 * @param requestTimeout maximum time in seconds to wait for a proxied request to complete.
 * @param isJmxEnabled whether JMX should be enabled; disable for higher performance.
 */
data class ServerConfiguration(
    val baseUri: URI = URI.create("http://0.0.0.0:8085"),
    val requestTimeout: Int = 30,
    val isJmxEnabled: Boolean = false,
)
