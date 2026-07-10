package org.radarbase.gateway.config

import java.net.URI

data class ServerConfiguration(
    val baseUri: URI = URI.create("http://0.0.0.0:8085"),
    val requestTimeout: Int = 30,
    val isJmxEnabled: Boolean = false,
)
