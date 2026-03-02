package org.radarbase.user.config

data class MPConfig(
        val baseUrl: String = "http://management-portal:8080",
        val timeoutSeconds: Long = 5,
        val maxRetries: Int = 3,
        val endpoints: ManagementPortalEndpoints = ManagementPortalEndpoints(),
    ) {
        data class ManagementPortalEndpoints(
            val users: String = "/api/projects/{projectId}/users",
            val participants: String = "/api/projects/{projectId}/subjects",
            val participant: String = "/api/subjects",
            /**
             * This endpoint is normally called by Kratos webhook handlers.
             * When we create identities via the Kratos admin API, that webhook may not fire,
             * so we call this manually to keep Management Portal in sync.
             */
            val kratosSubjectsWebhook: String = "/api/kratos/subjects",
            // Change to below when MP is migrated
            // val kratosSubjectsWebhook: String = "/api/webhook/kratos/subjects",
            val health: String = "/management/health",
        )
    }
