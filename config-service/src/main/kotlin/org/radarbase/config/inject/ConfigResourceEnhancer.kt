package org.radarbase.config.inject

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.config.config.ConfigServiceConfig
import org.radarbase.config.provider.AppConfigStudyConfigProvider
import org.radarbase.config.provider.DbStudyConfigProvider
import org.radarbase.config.provider.GithubStudyConfigProvider
import org.radarbase.config.resource.ConfigResource
import org.radarbase.config.service.StudyConfigService
import org.radarbase.config.service.StudyConfigServiceImpl
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.filter.Filters

class ConfigResourceEnhancer(
    private val config: ConfigServiceConfig,
) : JerseyResourceEnhancer {
    override val packages: Array<String> =
        arrayOf(
            "org.radarbase.config.resource",
        )

    override val classes: Array<Class<*>> =
        arrayOf(
            Filters.logResponse,
            ConfigResource::class.java,
        )

    override fun AbstractBinder.enhance() {
        bind(config)
            .to(ConfigServiceConfig::class.java)
            .`in`(Singleton::class.java)

        // Providers
        bind(GithubStudyConfigProvider::class.java)
            .to(GithubStudyConfigProvider::class.java)
            .`in`(Singleton::class.java)

        bind(AppConfigStudyConfigProvider::class.java)
            .to(AppConfigStudyConfigProvider::class.java)
            .`in`(Singleton::class.java)

        bind(DbStudyConfigProvider::class.java)
            .to(DbStudyConfigProvider::class.java)
            .`in`(Singleton::class.java)

        // Aggregate provider list is constructed inside StudyConfigServiceImpl using config.providers.order
        bind(StudyConfigServiceImpl::class.java)
            .to(StudyConfigService::class.java)
            .`in`(Singleton::class.java)

        bind(ConfigResource::class.java)
            .to(ConfigResource::class.java)
            .`in`(Singleton::class.java)
    }
}
