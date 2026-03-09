package org.radarbase.delegate.config

/**
 * Base URLs for each downstream microservice, used by the contract layer.
 */
data class ContractConfig(
    val user: String,
    val participant: String,
    val project: String,
    val config: String,
    val dataSources: String,
)

