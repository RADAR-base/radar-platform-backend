package org.radarbase.delegate.inject

import org.radarbase.delegate.config.DelegateConfig
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.auth.MPConfig
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer

class DelegateJwtEnhancerFactory(
    private val config: DelegateConfig,
) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> {
        val authConfig =
            AuthConfig(
                managementPortal = MPConfig(url = config.auth.managementPortal.url),
                jwtResourceName = config.auth.resourceName,
                jwtIssuer = config.auth.issuer,
                jwtECPublicKeys = config.auth.publicKeys?.ecdsa ?: emptyList(),
                jwtRSAPublicKeys = config.auth.publicKeys?.rsa ?: emptyList(),
                jwtKeystoreAlias = config.auth.keyStore?.alias,
                jwtKeystorePassword = config.auth.keyStore?.password,
                jwtKeystorePath = config.auth.keyStore?.path,
                jwksUrls = config.auth.publicKeyUrls ?: emptyList(),
            )
        return listOf(
            DelegateResourceEnhancer(config),
            Enhancers.radar(authConfig),
            Enhancers.ecdsa,
            Enhancers.exception,
        )
    }
}
