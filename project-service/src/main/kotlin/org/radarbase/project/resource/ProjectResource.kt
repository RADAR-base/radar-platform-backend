package org.radarbase.project.resource

import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.project.config.ProjectServiceConfiguration
import org.radarbase.project.model.CreateGroupRequest
import org.radarbase.project.service.ProjectService
import org.slf4j.LoggerFactory
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

@Path("/project-service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
class ProjectResource
@Inject
constructor(
    private val projectService: ProjectService,
    private val asyncCoroutineService: AsyncCoroutineService,
    private val config: ProjectServiceConfiguration,
) {
    private val logger = LoggerFactory.getLogger(ProjectResource::class.java)
    private val timeout = 5000L.toDuration(SECONDS)

    /**
     * Extracts bearer token from HttpHeaders.
     * Strips the "Bearer " prefix if present and returns the raw token.
     */
    private fun extractBearerToken(headers: HttpHeaders): String? {
        val header = headers.getHeaderString(HttpHeaders.AUTHORIZATION) ?: return null
        return if (header.startsWith("Bearer ", ignoreCase = true)) {
            header.substring(7).trim().takeIf { it.isNotEmpty() }
        } else {
            header.takeIf { it.isNotEmpty() }
        }
    }

    @GET
    @Path("/projects")
    fun getProjects(
        @Context headers: HttpHeaders,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        val authToken = extractBearerToken(headers)
        try {
            asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                projectService.getProjects(authToken)
            }
        } catch (e: Exception) {
            logger.error("Error fetching projects", e)
            asyncResponse.resume(
                Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error fetching projects: ${e.message}")
                    .build(),
            )
        }
    }

    @GET
    @Path("/projects/{projectId}")
    fun getProject(
        @PathParam("projectId") projectId: Long,
        @Context headers: HttpHeaders,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        val authToken = extractBearerToken(headers)
        try {
            asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                projectService.getProject(projectId, authToken)
            }
        } catch (e: Exception) {
            logger.error("Error fetching project $projectId", e)
            asyncResponse.resume(
                Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error fetching project: ${e.message}")
                    .build(),
            )
        }
    }

    @GET
    @Path("/projects/{projectId}/participants")
    fun getProjectParticipants(
        @PathParam("projectId") projectId: Long,
        @Context headers: HttpHeaders,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        val authToken = extractBearerToken(headers)
        try {
            asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                projectService.getProjectParticipants(projectId, authToken)
            }
        } catch (e: Exception) {
            logger.error("Error fetching participants for project $projectId", e)
            asyncResponse.resume(
                Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error fetching participants: ${e.message}")
                    .build(),
            )
        }
    }

    @GET
    @Path("/projects/{projectId}/participants/{participantId}")
    fun getProjectParticipant(
        @PathParam("projectId") projectId: Long,
        @PathParam("participantId") participantId: String,
        @Context headers: HttpHeaders,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        val authToken = extractBearerToken(headers)
        try {
            asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                projectService.getProjectParticipant(projectId, participantId, authToken)
            }
        } catch (e: Exception) {
            logger.error("Error fetching participant $participantId for project $projectId", e)
            asyncResponse.resume(
                Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error fetching participant: ${e.message}")
                    .build(),
            )
        }
    }

    @GET
    @Path("/projects/{projectName}/groups")
    fun listGroups(
        @PathParam("projectName") projectName: String,
        @Context headers: HttpHeaders,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        val authToken = extractBearerToken(headers)
        try {
            asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                projectService.listGroups(projectName, authToken)
            }
        } catch (e: Exception) {
            logger.error("Error listing groups for project $projectName", e)
            asyncResponse.resume(
                Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error listing groups: ${e.message}")
                    .build(),
            )
        }
    }

    @POST
    @Path("/projects/{projectName}/groups")
    fun createGroup(
        @PathParam("projectName") projectName: String,
        @Context headers: HttpHeaders,
        createGroupRequest: CreateGroupRequest,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        val authToken = extractBearerToken(headers)
        try {
            asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                projectService.createGroup(projectName, createGroupRequest, authToken)
                    ?: throw IllegalStateException("Failed to create group")
            }
        } catch (e: Exception) {
            logger.error("Error creating group for project $projectName", e)
            asyncResponse.resume(
                Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error creating group: ${e.message}")
                    .build(),
            )
        }
    }

    @DELETE
    @Path("/projects/{projectName}/groups/{groupName}")
    fun deleteGroup(
        @PathParam("projectName") projectName: String,
        @PathParam("groupName") groupName: String,
        @jakarta.ws.rs.QueryParam("unlinkSubjects") unlinkSubjects: Boolean?,
        @Context headers: HttpHeaders,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        val authToken = extractBearerToken(headers)
        try {
            asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                val success = projectService.deleteGroup(
                    projectName,
                    groupName,
                    unlinkSubjects ?: false,
                    authToken,
                )
                if (!success) throw IllegalStateException("Failed to delete group")
            }
        } catch (e: Exception) {
            logger.error("Error deleting group $groupName for project $projectName", e)
            asyncResponse.resume(
                Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error deleting group: ${e.message}")
                    .build(),
            )
        }
    }
}
