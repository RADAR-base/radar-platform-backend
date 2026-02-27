package org.radarbase.user.client

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.radarbase.core.util.KtorClientFactory
import org.radarbase.core.util.ServiceTokenProvider
import org.radarbase.user.config.UserServiceConfig
import org.radarbase.user.model.KratosCreateIdentityRequest
import org.radarbase.user.model.KratosRecoveryFlow
import org.radarbase.user.model.KratosRecoverySubmitRequest
import org.radarbase.user.model.KratosUser
import org.slf4j.LoggerFactory

@Singleton
class KratosClient @Inject constructor(
    private val config: UserServiceConfig,
    private val tokenProvider: ServiceTokenProvider,
    ktorClientFactory: KtorClientFactory,
    ) {
        private val logger = LoggerFactory.getLogger(KratosClient::class.java)
        private val kratosConfig = config.kratos

        private val client =
            ktorClientFactory.createClient(
                baseUrl = kratosConfig.baseUrl,
                timeoutSeconds = kratosConfig.timeoutSeconds,
                maxRetriesCount = kratosConfig.maxRetries,
            )

        suspend fun checkHealth(): Boolean {
            logger.debug("Checking Kratos health")
            return try {
                val response =
                    client
                        .get {
                            url("${config.kratos.baseUrl}${config.kratos.endpoints.health}")
                            header("Accept", "application/json")
                        }.body<String>()
                Json
                    .parseToJsonElement(response)
                    .jsonObject["status"]
                    ?.jsonPrimitive
                    ?.content == "ok"
            } catch (e: Exception) {
                logger.error("Failed to check Kratos health", e)
                false
            }
        }

        suspend fun getIdentities(): Array<KratosUser> {
            logger.debug("Fetching all identities from Kratos")
            return try {
                val token = tokenProvider.getToken()
                client
                    .get {
                        url("${config.kratos.baseUrl}${kratosConfig.endpoints.identities}")
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }.body()
            } catch (e: Exception) {
                logger.error("Failed to fetch identities from Kratos", e)
                emptyArray()
            }
        }

        suspend fun getIdentity(identityId: String): KratosUser? {
            logger.debug("Fetching identity $identityId from Kratos")
            return try {
                val token = tokenProvider.getToken()
                client
                    .get {
                        url("${config.kratos.baseUrl}${kratosConfig.endpoints.identity.replace("{id}", identityId)}")
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }.body()
            } catch (e: Exception) {
                logger.error("Failed to fetch identity $identityId from Kratos", e)
                null
            }
        }

        suspend fun getIdentityByEmail(email: String): KratosUser? {
            logger.debug("Fetching identity by email $email from Kratos")
            return try {
                val token = tokenProvider.getToken()
                client
                    .get {
                        url("${config.kratos.baseUrl}${kratosConfig.endpoints.identities}")
                        parameter("email", email)
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }.body<Array<KratosUser>>()
                    .firstOrNull()
            } catch (e: Exception) {
                logger.error("Failed to fetch identity by email $email from Kratos", e)
                null
            }
        }

        /**
         * Create an identity in Kratos.
         *
         * Note: creating identities via the admin API may not trigger Kratos webhooks,
         * so callers may need to manually notify downstream systems (e.g. Management Portal).
         */
        suspend fun createIdentity(request: KratosCreateIdentityRequest): KratosUser? {
            logger.debug("Creating identity in Kratos (schemaId={})", request.schemaId)
            return try {
                val token = tokenProvider.getToken()
                client
                    .post {
                        url("${config.kratos.baseUrl}${kratosConfig.endpoints.identities}")
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }.body()
            } catch (e: Exception) {
                logger.error("Failed to create identity in Kratos", e)
                null
            }
        }

        /**
         * Update an identity in Kratos
         * @param identityId The identity ID to update
         * @param updatedIdentity The updated identity object
         * @return The updated identity or null if update failed
         */
        suspend fun updateIdentity(
            identityId: String,
            updatedIdentity: KratosUser,
        ): KratosUser? {
            logger.debug("Updating identity $identityId in Kratos")
            return try {
                val token = tokenProvider.getToken()
                client
                    .put {
                        url("${config.kratos.baseUrl}${kratosConfig.endpoints.identity.replace("{id}", identityId)}")
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(updatedIdentity)
                    }.body<KratosUser>()
            } catch (e: Exception) {
                logger.error("Failed to update identity $identityId in Kratos", e)
                null
            }
        }

        /**
         * Trigger a Kratos recovery email for the provided email address.
         *
         * This uses the Kratos self-service recovery API flow:
         * 1) GET /self-service/recovery/api  → flow id
         * 2) POST /self-service/recovery?flow=<id> with { email, method }
         */
        suspend fun triggerRecoveryEmail(
            email: String,
            method: String = "link",
        ): Boolean {
            logger.debug("Triggering Kratos recovery email for {}", email)
            return try {
                val token = tokenProvider.getToken()
                val flow =
                    client
                        .get {
                            url("${config.kratos.baseUrl}${kratosConfig.endpoints.recoveryApi}")
                            header(HttpHeaders.Authorization, "Bearer $token")
                        }.body<KratosRecoveryFlow>()

                val response =
                    client.post {
                        url("${config.kratos.baseUrl}${kratosConfig.endpoints.recovery}")
                        parameter("flow", flow.id)
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(KratosRecoverySubmitRequest(email = email, method = method))
                    }

                response.status.value in 200..299
            } catch (e: Exception) {
                logger.error("Failed to trigger Kratos recovery email for $email", e)
                false
            }
        }
    }
