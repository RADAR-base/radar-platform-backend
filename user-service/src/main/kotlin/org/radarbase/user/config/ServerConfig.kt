package org.radarbase.user.config

data class ServerConfig(
    val baseUri: String = "http://0.0.0.0:8081",
    val isJmxEnabled: Boolean = true,
    val requestTimeout: Long = 30,
)
