package org.radarbase.datasources.config

import jakarta.inject.Singleton
import kotlinx.serialization.Serializable
import org.radarbase.core.config.LoggingConfig
import org.radarbase.core.config.ServiceAuthConfig
import java.net.URI

@Singleton
data class DataSourcesServiceConfig(
    val server: ServerConfig = ServerConfig(),
    val logging: LoggingConfig = LoggingConfig(),
    val serviceAuth: ServiceAuthConfig = ServiceAuthConfig(),
    val managementPortal: ManagementPortalConfig = ManagementPortalConfig(),
    val restSources: RestSourcesConfig = RestSourcesConfig(),
    val sources: List<DeviceConfig> = emptyList(),
) {
    /**
     * Device/source type definition. id: kebab-case; category: mobile | wearable | sensor | iot;
     * integration: kebab-case id (e.g. rest-api-connector, passive-app-prmt, radar-iot-module);
     * dataTypes: lowercase_with_underscores.
     */
    @Serializable
    data class DeviceConfig(
        val id: String,
        val name: String,
        val category: String,
        val integration: String,
        val dataTypes: List<String> = emptyList(),
        val description: String? = null,
    )

    data class ServerConfig(
        val baseUri: String = "http://0.0.0.0:8084",
        val isJmxEnabled: Boolean = true,
    )

    data class ManagementPortalConfig(
        val baseUrl: String = "http://management-portal:8080",
        val timeoutSeconds: Long = 5,
        val maxRetries: Int = 3,
        val endpoints: ManagementPortalEndpoints = ManagementPortalEndpoints(),
    ) {
        data class ManagementPortalEndpoints(
            val participantSources: String = "/api/subjects/{participantId}/sources",
        )
    }

    data class RestSourcesConfig(
        val baseUrl: String = "http://rest-sources-backend:8080/rest-sources/backend",
        val timeoutSeconds: Long = 5,
        val maxRetries: Int = 3,
        val endpoints: RestSourcesEndpoints = RestSourcesEndpoints(),
    ) {
        data class RestSourcesEndpoints(
            val users: String = "/users",
        )
    }

    fun validate() {
        check(URI(server.baseUri).host != null) { "Invalid baseUri: ${server.baseUri}" }
        check(
            URI(managementPortal.baseUrl).host != null,
        ) { "Invalid managementPortalBaseUrl: ${managementPortal.baseUrl}" }
        check(URI(restSources.baseUrl).host != null) { "Invalid restSourcesBaseUrl: ${restSources.baseUrl}" }
    }
}
