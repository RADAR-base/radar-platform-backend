package org.radarbase.delegate.config

data class PublicKeys(
    val ecdsa: List<String>,
    val rsa: List<String>,
)
