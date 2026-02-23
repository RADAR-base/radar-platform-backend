package org.radarbase.delegate.config

import org.radarbase.delegate.inject.DelegateJwtEnhancerFactory
import org.radarbase.jersey.enhancer.EnhancerFactory

data class DelegateConfig(
    val auth: AuthConfig,
    val userService: UserServiceConfig,
    val participantService: ParticipantServiceConfig,
    val projectService: ProjectServiceConfig,
    val configService: ConfigServiceConfig,
    val dataSourcesService: DataSourcesServiceConfig,
) {
    val resourceConfig: Class<out EnhancerFactory> = DelegateJwtEnhancerFactory::class.java
}

data class AuthConfig(
    val managementPortal: MPConfig,
    val resourceName: String,
    val issuer: String,
    val publicKeys: PublicKeys?,
    val keyStore: KeyStoreConfig?,
    val publicKeyUrls: List<String>? = emptyList(),
)

data class MPConfig(
    val url: String,
)

data class PublicKeys(
    val ecdsa: List<String>,
    val rsa: List<String>,
)

data class KeyStoreConfig(
    val alias: String,
    val password: String,
    val path: String,
)

data class UserServiceConfig(
    val baseUrl: String,
)

data class ParticipantServiceConfig(
    val baseUrl: String,
)

data class ProjectServiceConfig(
    val baseUrl: String,
)

data class ConfigServiceConfig(
    val baseUrl: String,
)

data class DataSourcesServiceConfig(
    val baseUrl: String,
)
