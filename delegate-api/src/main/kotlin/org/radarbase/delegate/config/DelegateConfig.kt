package org.radarbase.delegate.config

import org.radarbase.delegate.inject.DelegateJwtEnhancerFactory
import org.radarbase.jersey.enhancer.EnhancerFactory

data class DelegateConfig(
    val auth: AuthConfig,
    val contract: ContractConfig,
) {
    val resourceConfig: Class<out EnhancerFactory> = DelegateJwtEnhancerFactory::class.java
}
