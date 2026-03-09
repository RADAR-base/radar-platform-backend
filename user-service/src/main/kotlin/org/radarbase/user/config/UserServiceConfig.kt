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
    val managementPortal: MPConfig = MPConfig(),
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
}
