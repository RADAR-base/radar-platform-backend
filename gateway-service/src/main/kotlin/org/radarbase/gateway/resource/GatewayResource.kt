package org.radarbase.gateway.resource

import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.glassfish.jersey.media.multipart.FormDataContentDisposition
import org.glassfish.jersey.media.multipart.FormDataParam
import org.radarbase.gateway.config.GatewayServiceConfiguration
import org.radarbase.gateway.model.GatewayProxyResponse
import org.radarbase.gateway.service.GatewayService
import org.radarbase.jersey.service.AsyncCoroutineService
import org.slf4j.LoggerFactory
import java.io.InputStream
import kotlin.time.Duration.Companion.seconds

@Path("/gateway-service")
@Singleton
class GatewayResource
@Inject
constructor(
    private val gatewayService: GatewayService,
    private val asyncCoroutineService: AsyncCoroutineService,
    config: GatewayServiceConfiguration,
) {
    private val logger = LoggerFactory.getLogger(GatewayResource::class.java)
    private val timeout = config.server.requestTimeout.seconds

    @GET
    @Path("/topics")
    fun listTopics(
        @Context headers: HttpHeaders,
        @Suspended asyncResponse: AsyncResponse,
    ) = runProxy(asyncResponse, "listing topics") {
        gatewayService.listTopics(extractBearerToken(headers))
    }

    @GET
    @Path("/topics/{topicName}")
    fun getTopic(
        @PathParam("topicName") topicName: String,
        @Context headers: HttpHeaders,
        @Suspended asyncResponse: AsyncResponse,
    ) = runProxy(asyncResponse, "fetching topic $topicName") {
        gatewayService.getTopic(topicName, extractBearerToken(headers))
    }

    @POST
    @Path("/topics/{topicName}")
    @Consumes(MediaType.WILDCARD)
    fun produceToTopic(
        @PathParam("topicName") topicName: String,
        body: ByteArray?,
        @Context headers: HttpHeaders,
        @Suspended asyncResponse: AsyncResponse,
    ) = runProxy(asyncResponse, "producing to topic $topicName") {
        gatewayService.produceToTopic(
            topic = topicName,
            contentType = headers.getHeaderString(HttpHeaders.CONTENT_TYPE),
            body = body ?: ByteArray(0),
            authToken = extractBearerToken(headers),
        )
    }

    @POST
    @Path("/{projectId}/{subjectId}/{topic}/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun uploadFile(
        @FormDataParam("file") fileInputStream: InputStream,
        @FormDataParam("file") fileInfo: FormDataContentDisposition,
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("topic") topic: String,
        @Context headers: HttpHeaders,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        val content = fileInputStream.readBytes()
        runProxy(asyncResponse, "uploading file for subject $subjectId in project $projectId") {
            gatewayService.uploadFile(
                projectId = projectId,
                subjectId = subjectId,
                topic = topic,
                fileName = fileInfo.fileName,
                content = content,
                authToken = extractBearerToken(headers),
            )
        }
    }

    private fun runProxy(
        asyncResponse: AsyncResponse,
        action: String,
        block: suspend () -> GatewayProxyResponse,
    ) {
        try {
            asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                block().toJakartaResponse()
            }
        } catch (e: Exception) {
            logger.error("Error while $action", e)
            asyncResponse.resume(
                Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error while $action: ${e.message}")
                    .build(),
            )
        }
    }

    private fun GatewayProxyResponse.toJakartaResponse(): Response {
        val builder = Response.status(status)
        contentType?.let { builder.type(it) }
        location?.let { builder.header(HttpHeaders.LOCATION, it) }
        body?.let { builder.entity(it) }
        return builder.build()
    }

    /**
     * Extracts the bearer token from [HttpHeaders], stripping the "Bearer " prefix if present.
     */
    private fun extractBearerToken(headers: HttpHeaders): String? {
        val header = headers.getHeaderString(HttpHeaders.AUTHORIZATION) ?: return null
        return if (header.startsWith("Bearer ", ignoreCase = true)) {
            header.substring(7).trim().takeIf { it.isNotEmpty() }
        } else {
            header.takeIf { it.isNotEmpty() }
        }
    }
}
