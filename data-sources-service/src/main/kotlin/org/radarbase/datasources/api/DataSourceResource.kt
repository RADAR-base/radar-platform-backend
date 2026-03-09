package org.radarbase.datasources.api

import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.radarbase.datasources.config.DataSourcesServiceConfig
import org.radarbase.datasources.service.DataSourcesService

@Path("/data-sources-service")
class DataSourceResource
@Inject constructor(
    private val dataSourcesService: DataSourcesService,
    private val config: DataSourcesServiceConfig,
) {
    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    fun healthCheck(): Response = Response.ok(mapOf("status" to "healthy")).build()

    @GET
    @Path("/sources")
    @Produces(MediaType.APPLICATION_JSON)
    fun getSourceCatalog(): Response {
        val json = Json { ignoreUnknownKeys = true }
        val body = json.encodeToString(config.sources)
        return Response.ok(body, MediaType.APPLICATION_JSON).build()
    }

    @GET
    @Path("/projects/{projectId}/participants/{participantId}/sources")
    @Produces(MediaType.APPLICATION_JSON)
    fun getParticipantSources(
        @PathParam("projectId") projectId: String,
        @PathParam("participantId") participantId: String,
    ): Response {
        val body = runBlocking {
            dataSourcesService.getParticipantSources(projectId, participantId)
        }
        return Response.ok(body, MediaType.APPLICATION_JSON).build()
    }
}
