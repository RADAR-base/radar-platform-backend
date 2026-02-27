package org.radarbase.delegate.config

data class AuthConfig(
    val managementPortal: MPConfig,
    val resourceName: String,
    val issuer: String,
    val publicKeys: PublicKeys?,
    val keyStore: KeyStoreConfig?,
    val publicKeyUrls: List<String>? = emptyList(),
)


