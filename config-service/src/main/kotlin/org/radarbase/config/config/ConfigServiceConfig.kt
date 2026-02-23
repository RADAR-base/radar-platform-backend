package org.radarbase.config.config

import jakarta.inject.Singleton
import org.radarbase.config.inject.ConfigJwtEnhancerFactory
import org.radarbase.jersey.enhancer.EnhancerFactory
import java.net.URI

@Singleton
data class ConfigServiceConfig(
    val server: ServerConfig = ServerConfig(),
    val logging: LoggingConfig = LoggingConfig(),
    val providers: ProviderConfig = ProviderConfig(),
) {
    val resourceConfig: Class<out EnhancerFactory> = ConfigJwtEnhancerFactory::class.java

    fun validate() {
        check(URI(server.baseUri).host != null) { "Invalid baseUri: ${server.baseUri}" }
    }

    data class ServerConfig(
        val baseUri: String = "http://0.0.0.0:8083",
        val isJmxEnabled: Boolean = true,
    )

    data class LoggingConfig(
        val level: String = "INFO",
        val file: String = "logs/config-service.log",
        val pattern: String = "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n",
    )

    data class ProviderConfig(
        val github: GithubProviderConfig = GithubProviderConfig(),
        val appConfig: AppConfigProviderConfig = AppConfigProviderConfig(),
        val db: DbProviderConfig = DbProviderConfig(),
        /** Order of providers for read and write (first enabled writable provider is used for writes). */
        val order: List<String> = listOf("github", "appConfig", "db"),
    )

    data class GithubProviderConfig(
        val enabled: Boolean = true,
        val baseUrl: String = "https://raw.githubusercontent.com/RADAR-base/study-definitions/main",
        /** GitHub API repo owner (for writes). */
        val owner: String = "RADAR-base",
        /** GitHub API repo name (for writes). */
        val repo: String = "study-definitions",
        /** Branch to read from and write to. */
        val branch: String = "main",
        /** Token with repo scope (required for writes). */
        val token: String? = null,
        /** Separate repo for questionnaire bodies (aRMT definitions). */
        val questionnaireRepoOwner: String = "RADAR-base",
        val questionnaireRepo: String = "RADAR-REDCap-aRMT-Definitions",
        val questionnaireRepoBranch: String = "master",
        /** Optional separate repo for protocol body. When set, protocol is read/written here instead of main repo. */
        val protocolRepoOwner: String? = null,
        val protocolRepo: String? = null,
        val protocolRepoBranch: String? = null,
        /** Optional separate repo for enrolment (landingpage.json + protocol.json). When set, enrolment is read/written here. */
        val enrolmentRepoOwner: String? = null,
        val enrolmentRepo: String? = null,
        val enrolmentRepoBranch: String? = null,
    )

    data class AppConfigProviderConfig(
        val enabled: Boolean = false,
        val basePath: String = "",
    )

    data class DbProviderConfig(
        val enabled: Boolean = false,
        val jdbcUrl: String = "",
        val username: String = "",
        val password: String = "",
    )
}
