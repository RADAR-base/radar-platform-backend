package org.radarbase.gateway.client

import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.radarbase.core.util.KtorClientFactory
import org.radarbase.core.util.ServiceTokenProvider
import org.radarbase.gateway.config.GatewayServiceConfiguration
import org.radarbase.gateway.model.GatewayProxyResponse
import org.slf4j.LoggerFactory

@Singleton
class RadarGatewayClient @Inject constructor(
    private val serviceTokenProvider: ServiceTokenProvider,
    config: GatewayServiceConfiguration,
    ktorClientFactory: KtorClientFactory,
) {
    private val logger = LoggerFactory.getLogger(RadarGatewayClient::class.java)
    private val gatewayConfig = config.gateway

    private val client = ktorClientFactory.createClient(
        baseUrl = gatewayConfig.baseUrl,
        timeoutSeconds = gatewayConfig.timeout,
        maxRetriesCount = gatewayConfig.maxRetries,
    )

    suspend fun checkHealth(): Boolean {
        logger.debug("Checking RADAR-Gateway health")
        return try {
            val response = client.get {
                url("${gatewayConfig.baseUrl}/liveness")
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            logger.error("Failed to check RADAR-Gateway health", e)
            false
        }
    }

    suspend fun listTopics(authToken: String? = null): GatewayProxyResponse {
        logger.debug("Listing topics from RADAR-Gateway")
        val topicsUrl = "${gatewayConfig.baseUrl}/topics"
        return proxy(topicsUrl, authToken) { token ->
            client.get {
                url(topicsUrl)
                bearer(token)
            }
        }
    }

    suspend fun getTopic(
        topic: String,
        authToken: String? = null,
    ): GatewayProxyResponse {
        logger.debug("Fetching topic {} from RADAR-Gateway", topic)
        val topicUrl = "${gatewayConfig.baseUrl}/topics/$topic"
        return proxy(topicUrl, authToken) { token ->
            client.get {
                url(topicUrl)
                bearer(token)
            }
        }
    }

    suspend fun produceToTopic(
        topic: String,
        requestContentType: String?,
        body: ByteArray,
        authToken: String? = null,
    ): GatewayProxyResponse {
        logger.debug("Producing {} bytes to topic {} via RADAR-Gateway", body.size, topic)
        val topicUrl = "${gatewayConfig.baseUrl}/topics/$topic"
        return proxy(topicUrl, authToken) { token ->
            client.post {
                url(topicUrl)
                bearer(token)
                contentType(parseContentType(requestContentType))
                setBody(body)
            }
        }
    }

    suspend fun uploadFile(
        projectId: String,
        subjectId: String,
        topic: String,
        fileName: String,
        content: ByteArray,
        authToken: String? = null,
    ): GatewayProxyResponse {
        val uploadUrl = "${gatewayConfig.baseUrl}/$projectId/$subjectId/$topic/upload"
        logger.debug("Uploading file {} for subject {} in project {} via RADAR-Gateway", fileName, subjectId, projectId)
        return proxy(uploadUrl, authToken) { token ->
            client.post {
                url(uploadUrl)
                bearer(token)
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                key = "file",
                                value = content,
                                headers = Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                },
                            )
                        },
                    ),
                )
            }
        }
    }

    private fun HttpRequestBuilder.bearer(token: String?) {
        token ?: return
        header(HttpHeaders.Authorization, "Bearer $token")
    }

    private fun parseContentType(value: String?): ContentType =
        value?.let {
            runCatching { ContentType.parse(it) }.getOrDefault(ContentType.Application.Json)
        } ?: ContentType.Application.Json

    /**
     * Resolves the auth token (forwarded token, else service token) and captures the upstream
     * response as a [GatewayProxyResponse]. Any transport failure is surfaced as a 502.
     */
    private suspend fun proxy(
        target: String,
        authToken: String?,
        request: suspend (token: String?) -> HttpResponse,
    ): GatewayProxyResponse = try {
        val token = authToken ?: runCatching { serviceTokenProvider.getToken() }.getOrNull()
        val response = request(token)
        GatewayProxyResponse(
            status = response.status.value,
            contentType = response.headers[HttpHeaders.ContentType],
            location = response.headers[HttpHeaders.Location],
            body = response.body(),
        )
    } catch (e: Exception) {
        logger.error("Failed to reach RADAR-Gateway at {}", target, e)
        GatewayProxyResponse(status = 502, body = "Failed to reach gateway: ${e.message}".toByteArray())
    }
}
