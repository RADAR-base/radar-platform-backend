package org.radarbase.delegate.api

import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.runBlocking
import org.radarbase.auth.authorization.Permission
import org.radarbase.delegate.service.DelegateApiService
import org.radarbase.delegate.service.DelegateHealthMetric
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission

@Path("/api/v2/delegate")
class DelegateResource {
    @Inject
    lateinit var healthMetric: DelegateHealthMetric

    @Inject
    lateinit var apiService: DelegateApiService

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    fun healthCheck(): Response = Response.ok(runBlocking { healthMetric.computeMetrics() }).build()

    @GET
    @Path("/health/status")
    @Produces(MediaType.APPLICATION_JSON)
    fun healthStatus(): Response = Response.ok(runBlocking { healthMetric.computeStatus() }).build()

    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    fun users(
        @QueryParam("projectId") projectId: String?,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        if (projectId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Project ID is required").build()
        }
        val authToken = extractToken(httpHeaders)
        return Response.ok(runBlocking { apiService.getUsers(projectId, authToken) }).build()
    }

    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    @GET
    @Path("/users/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun user(
        @QueryParam("projectId") projectId: String? = null,
        @PathParam("userId") userId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        if (projectId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Project ID is required").build()
        }
        val authToken = extractToken(httpHeaders)
        return Response.ok(runBlocking { apiService.getUser(projectId, userId, authToken) }).build()
    }

    @Authenticated
    @NeedsPermission(Permission.PROJECT_UPDATE)
    @POST
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createUser(
        @QueryParam("projectId") projectId: String?,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        if (projectId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Project ID is required").build()
        }
        val authToken = extractToken(httpHeaders)
        val (status, responseBody) = runBlocking { apiService.createUser(projectId, body, authToken) }
        return Response
            .status(status)
            .entity(responseBody)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }

    @Authenticated
    @NeedsPermission(Permission.PROJECT_UPDATE)
    @PUT
    @Path("/users/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateUser(
        @QueryParam("projectId") projectId: String?,
        @PathParam("userId") userId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        if (projectId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Project ID is required").build()
        }
        val authToken = extractToken(httpHeaders)
        val (status, responseBody) = runBlocking { apiService.updateUser(projectId, userId, body, authToken) }
        return Response
            .status(status)
            .entity(responseBody)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }

    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    @GET
    @Path("/participants")
    @Produces(MediaType.APPLICATION_JSON)
    fun participants(
        @QueryParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        return Response.ok(runBlocking { apiService.getParticipants(projectId, authToken) }).build()
    }

    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    @GET
    @Path("/participants/{participantId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun participant(
        @QueryParam("projectId") projectId: String?,
        @PathParam("participantId") participantId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        if (projectId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Project ID is required").build()
        }
        val authToken = extractToken(httpHeaders)
        return Response.ok(runBlocking { apiService.getParticipant(projectId, participantId, authToken) }).build()
    }

    @Authenticated
    @NeedsPermission(Permission.PROJECT_UPDATE)
    @POST
    @Path("/participants")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createParticipant(
        @QueryParam("projectId") projectId: String?,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        if (projectId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Project ID is required").build()
        }
        val authToken = extractToken(httpHeaders)
        val (status, responseBody) = runBlocking { apiService.createParticipant(projectId, body, authToken) }
        return Response
            .status(status)
            .entity(responseBody)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }

    @Authenticated
    @NeedsPermission(Permission.PROJECT_UPDATE)
    @PUT
    @Path("/participants/{participantId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateParticipant(
        @QueryParam("projectId") projectId: String?,
        @PathParam("participantId") participantId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        if (projectId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Project ID is required").build()
        }
        val authToken = extractToken(httpHeaders)
        val (status, responseBody) = runBlocking { apiService.updateParticipant(projectId, participantId, body, authToken) }
        return Response
            .status(status)
            .entity(responseBody)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }

    @GET
    @Path("/projects")
    @Produces(MediaType.APPLICATION_JSON)
    fun projects(
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        return Response.ok(runBlocking { apiService.getProjects(authToken) }).build()
    }

    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    @GET
    @Path("/projects/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun project(
        @PathParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        return Response.ok(runBlocking { apiService.getProject(projectId, authToken) }).build()
    }

    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    @GET
    @Path("/projects/{projectId}/participants")
    @Produces(MediaType.APPLICATION_JSON)
    fun projectParticipants(
        @PathParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        return Response.ok(runBlocking { apiService.getProjectParticipants(projectId, authToken) }).build()
    }

    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    @GET
    @Path("/projects/{projectId}/participants/{participantId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun projectParticipant(
        @PathParam("projectId") projectId: String,
        @PathParam("participantId") participantId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        return Response
            .ok(
                runBlocking { apiService.getProjectParticipant(projectId, participantId, authToken) },
            ).build()
    }

    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    @GET
    @Path("/projects/{projectName}/groups")
    @Produces(MediaType.APPLICATION_JSON)
    fun listGroups(
        @PathParam("projectName") projectName: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        return Response.ok(runBlocking { apiService.listGroups(projectName, authToken) }).build()
    }

    @POST
    @Path("/projects/{projectName}/groups")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createGroup(
        @PathParam("projectName") projectName: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val (status, responseBody) = runBlocking { apiService.createGroup(projectName, body, authToken) }
        return Response
            .status(status)
            .entity(responseBody)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }

    @DELETE
    @Path("/projects/{projectName}/groups/{groupName}")
    @Produces(MediaType.APPLICATION_JSON)
    fun deleteGroup(
        @PathParam("projectName") projectName: String,
        @PathParam("groupName") groupName: String,
        @QueryParam("unlinkSubjects") unlinkSubjects: Boolean?,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val (status, responseBody) =
            runBlocking {
                apiService.deleteGroup(projectName, groupName, unlinkSubjects ?: false, authToken)
            }
        return Response
            .status(status)
            .entity(responseBody)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }

    @GET
    @Path("/sources")
    @Produces(MediaType.APPLICATION_JSON)
    fun sourceCatalog(): Response {
        val body = runBlocking { apiService.getSourceCatalog() }
        return Response.ok(body, MediaType.APPLICATION_JSON).build()
    }

    @GET
    @Path("/sources/projects/{projectId}/participants/{participantId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun participantSources(
        @PathParam("projectId") projectId: String,
        @PathParam("participantId") participantId: String,
    ): Response {
        val body =
            runBlocking {
                apiService.getParticipantSources(projectId, participantId)
            }
        return Response.ok(body, MediaType.APPLICATION_JSON).build()
    }

    @GET
    @Path("/config/projects/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun studyConfig(
        @PathParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val body = runBlocking { apiService.getStudyConfig(projectId, authToken) }
        return Response.ok(body, MediaType.APPLICATION_JSON).build()
    }

    @GET
    @Path("/config/projects/{projectId}/protocol")
    @Produces(MediaType.APPLICATION_JSON)
    fun studyProtocol(
        @PathParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val body = runBlocking { apiService.getProtocol(projectId, authToken) }
        return Response.ok(body, MediaType.APPLICATION_JSON).build()
    }

    @GET
    @Path("/config/projects/{projectId}/sources/enrolment")
    @Produces(MediaType.APPLICATION_JSON)
    fun studyEnrolmentSource(
        @PathParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val body = runBlocking { apiService.getEnrolmentSource(projectId, authToken) }
        return Response.ok(body, MediaType.APPLICATION_JSON).build()
    }

    @GET
    @Path("/config/projects/{projectId}/enrolment/landing")
    @Produces(MediaType.APPLICATION_JSON)
    fun studyEnrolmentLanding(
        @PathParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val body = runBlocking { apiService.getEnrolmentLanding(projectId, authToken) }
        return Response.ok(body, MediaType.APPLICATION_JSON).build()
    }

    @GET
    @Path("/config/projects/{projectId}/enrolment/protocol")
    @Produces(MediaType.APPLICATION_JSON)
    fun studyEnrolmentProtocol(
        @PathParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val body = runBlocking { apiService.getEnrolmentProtocol(projectId, authToken) }
        return Response.ok(body, MediaType.APPLICATION_JSON).build()
    }

    @GET
    @Path("/config/projects/{projectId}/questionnaires")
    @Produces(MediaType.APPLICATION_JSON)
    fun studyQuestionnaires(
        @PathParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val body = runBlocking { apiService.getQuestionnaires(projectId, authToken) }
        return Response.ok(body, MediaType.APPLICATION_JSON).build()
    }

    @GET
    @Path("/config/projects/{projectId}/questionnaires/{questionnaireId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun studyQuestionnaire(
        @PathParam("projectId") projectId: String,
        @PathParam("questionnaireId") questionnaireId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val body = runBlocking { apiService.getQuestionnaire(projectId, questionnaireId, authToken) }
        return Response.ok(body, MediaType.APPLICATION_JSON).build()
    }

    @PUT
    @Path("/config/projects/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateStudyConfig(
        @PathParam("projectId") projectId: String,
        body: String?,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        if (body.isNullOrBlank()) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("Request body is required (StudyDefinition JSON)")
                .type(MediaType.APPLICATION_JSON)
                .build()
        }
        val authToken = extractToken(httpHeaders)
        val (status, responseBody) = runBlocking { apiService.updateStudyConfig(projectId, body, authToken) }
        return Response
            .status(status)
            .entity(responseBody)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }

    @PUT
    @Path("/config/projects/{projectId}/questionnaires")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateQuestionnaires(
        @PathParam("projectId") projectId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val (status, responseBody) = runBlocking { apiService.updateQuestionnaires(projectId, body, authToken) }
        return Response
            .status(status)
            .entity(responseBody)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }

    @POST
    @Path("/config/projects/{projectId}/questionnaires")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createQuestionnaire(
        @PathParam("projectId") projectId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val (status, responseBody) = runBlocking { apiService.createQuestionnaire(projectId, body, authToken) }
        return Response
            .status(status)
            .entity(responseBody)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }

    @PUT
    @Path("/config/projects/{projectId}/questionnaires/{questionnaireId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateQuestionnaire(
        @PathParam("projectId") projectId: String,
        @PathParam("questionnaireId") questionnaireId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val (status, responseBody) =
            runBlocking { apiService.updateQuestionnaire(projectId, questionnaireId, body, authToken) }
        return Response
            .status(status)
            .entity(responseBody)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }

    @DELETE
    @Path("/config/projects/{projectId}/questionnaires/{questionnaireId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun deleteQuestionnaire(
        @PathParam("projectId") projectId: String,
        @PathParam("questionnaireId") questionnaireId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val (status, responseBody) =
            runBlocking { apiService.deleteQuestionnaire(projectId, questionnaireId, authToken) }
        return Response
            .status(status)
            .entity(responseBody)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }

    @PUT
    @Path("/config/projects/{projectId}/sources/protocol")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateProtocolSource(
        @PathParam("projectId") projectId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val (status, responseBody) = runBlocking { apiService.updateProtocolSource(projectId, body, authToken) }
        return Response
            .status(status)
            .entity(responseBody)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }

    @PUT
    @Path("/config/projects/{projectId}/protocol")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateProtocolBody(
        @PathParam("projectId") projectId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val (status, responseBody) = runBlocking { apiService.updateProtocolBody(projectId, body, authToken) }
        return Response
            .status(status)
            .entity(responseBody)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }

    @PUT
    @Path("/config/projects/{projectId}/sources/enrolment")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateEnrolmentSource(
        @PathParam("projectId") projectId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val (status, responseBody) =
            runBlocking { apiService.updateEnrolmentSource(projectId, body, authToken) }
        return Response
            .status(status)
            .entity(responseBody)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }

    @PUT
    @Path("/config/projects/{projectId}/enrolment/landing")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateEnrolmentLandingBody(
        @PathParam("projectId") projectId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val (status, responseBody) =
            runBlocking { apiService.updateEnrolmentLandingBody(projectId, body, authToken) }
        return Response
            .status(status)
            .entity(responseBody)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }

    @PUT
    @Path("/config/projects/{projectId}/enrolment/protocol")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateEnrolmentProtocolBody(
        @PathParam("projectId") projectId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response {
        val authToken = extractToken(httpHeaders)
        val (status, responseBody) =
            runBlocking { apiService.updateEnrolmentProtocolBody(projectId, body, authToken) }
        return Response
            .status(status)
            .entity(responseBody)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }

    private fun extractToken(httpHeaders: HttpHeaders): String? {
        val authHeader = httpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION)?.firstOrNull()
        return if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authHeader.substring(7)
        } else {
            null
        }
    }
}
