package org.radarbase.delegate.config

/** Base URL reference for each downstream microservice used by the delegate-api. */
data class UserServiceConfig(val baseUrl: String)

data class ParticipantServiceConfig(val baseUrl: String)

data class ProjectServiceConfig(val baseUrl: String)

data class ConfigServiceConfig(val baseUrl: String)

data class DataSourcesServiceConfig(val baseUrl: String)
