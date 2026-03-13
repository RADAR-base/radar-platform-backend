package org.radarbase.user.config

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
