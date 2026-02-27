package org.radarbase.user.config

import jakarta.inject.Singleton
import org.radarbase.core.config.LoggingConfig
import org.radarbase.core.config.ServiceAuthConfig
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.user.inject.UserJwtEnhancerFactory
import java.net.URI

@Singleton
data class UserServiceConfig(
    val server: ServerConfig = ServerConfig(),
    val serviceAuth: ServiceAuthConfig = ServiceAuthConfig(),
    val kratos: KratosConfig = KratosConfig(),
    val managementPortal: ManagementPortalConfig = ManagementPortalConfig(),
    val logging: LoggingConfig = LoggingConfig(),
) {
    val resourceConfig: Class<out EnhancerFactory> = UserJwtEnhancerFactory::class.java

    fun validate() {
        check(URI(server.baseUri).host != null) { "Invalid baseUri: ${server.baseUri}" }
        check(URI(kratos.baseUrl).host != null) { "Invalid kratosBaseUrl: ${kratos.baseUrl}" }
        check(
            URI(managementPortal.baseUrl).host != null,
        ) { "Invalid managementPortalBaseUrl: ${managementPortal.baseUrl}" }
    }

    data class ServerConfig(
        val baseUri: String = "http://0.0.0.0:8081",
        val isJmxEnabled: Boolean = true,
        val requestTimeout: RequestTimeout = RequestTimeout(),
    )

    class RequestTimeout(
        val seconds: Long = 5000,
    )

    data class KratosConfig(
        val baseUrl: String = "http://kratos:4434",
        val timeoutSeconds: Long = 5,
        val maxRetries: Int = 3,
        val endpoints: KratosEndpoints = KratosEndpoints(),
    ) {
        data class KratosEndpoints(
            val identities: String = "/admin/kratos/identities",
            val identity: String = "/admin/kratos/identities/{id}",
            val health: String = "/kratos/health/ready",
            /**
             * API (non-browser) recovery flow init endpoint.
             * For deployments that are reverse-proxied under `/kratos`, this default should work.
             */
            val recoveryApi: String = "/kratos/self-service/recovery/api",
            /**
             * Recovery submit endpoint. The flow id is passed as `?flow=...`.
             */
            val recovery: String = "/kratos/self-service/recovery",
        )
    }

    data class ManagementPortalConfig(
        val baseUrl: String = "http://management-portal:8080",
        val timeoutSeconds: Long = 5,
        val maxRetries: Int = 3,
        val endpoints: ManagementPortalEndpoints = ManagementPortalEndpoints(),
    ) {
        data class ManagementPortalEndpoints(
            val users: String = "/api/projects/{projectId}/users",
            val participants: String = "/api/projects/{projectId}/subjects",
            val participant: String = "/api/subjects",
            /**
             * This endpoint is normally called by Kratos webhook handlers.
             * When we create identities via the Kratos admin API, that webhook may not fire,
             * so we call this manually to keep Management Portal in sync.
             */
            val kratosSubjectsWebhook: String = "/api/kratos/subjects",
            // Change to below when MP is migrated
            // val kratosSubjectsWebhook: String = "/api/webhook/kratos/subjects",
            val health: String = "/management/health",
        )
    }
}
