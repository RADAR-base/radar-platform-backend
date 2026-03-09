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
import org.radarbase.contract.response.ProxyResponse
import org.radarbase.contract.utils.ContractUtils.toJakartaResponse
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

    // ------------------------------------------------------------------ //
    //  Health
    // ------------------------------------------------------------------ //

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    fun healthCheck(): Response = Response.ok(runBlocking { healthMetric.computeMetrics() }).build()

    @GET
    @Path("/health/status")
    @Produces(MediaType.APPLICATION_JSON)
    fun healthStatus(): Response = Response.ok(runBlocking { healthMetric.computeStatus() }).build()

    // ------------------------------------------------------------------ //
    //  Users
    // ------------------------------------------------------------------ //

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
        return Response.ok(runBlocking { apiService.getUsers(projectId, extractToken(httpHeaders)) }).build()
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
        return Response.ok(runBlocking { apiService.getUser(projectId, userId, extractToken(httpHeaders)) }).build()
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
        return runBlocking { apiService.createUser(projectId, body, extractToken(httpHeaders)) }.toJakartaResponse()
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
        return runBlocking { apiService.updateUser(projectId, userId, body, extractToken(httpHeaders)) }.toJakartaResponse()
    }

    // ------------------------------------------------------------------ //
    //  Participants
    // ------------------------------------------------------------------ //

    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    @GET
    @Path("/participants")
    @Produces(MediaType.APPLICATION_JSON)
    fun participants(
        @QueryParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = Response.ok(runBlocking { apiService.getParticipants(projectId, extractToken(httpHeaders)) }).build()

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
        return Response.ok(runBlocking { apiService.getParticipant(projectId, participantId, extractToken(httpHeaders)) }).build()
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
        return runBlocking { apiService.createParticipant(projectId, body, extractToken(httpHeaders)) }.toJakartaResponse()
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
        return runBlocking { apiService.updateParticipant(projectId, participantId, body, extractToken(httpHeaders)) }.toJakartaResponse()
    }

    // ------------------------------------------------------------------ //
    //  Projects
    // ------------------------------------------------------------------ //

    @GET
    @Path("/projects")
    @Produces(MediaType.APPLICATION_JSON)
    fun projects(@Context httpHeaders: HttpHeaders): Response =
        runBlocking { apiService.getProjects(extractToken(httpHeaders)) }.toJakartaResponse()

    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    @GET
    @Path("/projects/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun project(
        @PathParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.getProject(projectId, extractToken(httpHeaders)) }.toJakartaResponse()

    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    @GET
    @Path("/projects/{projectId}/participants")
    @Produces(MediaType.APPLICATION_JSON)
    fun projectParticipants(
        @PathParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.getProjectParticipants(projectId, extractToken(httpHeaders)) }.toJakartaResponse()

    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    @GET
    @Path("/projects/{projectId}/participants/{participantId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun projectParticipant(
        @PathParam("projectId") projectId: String,
        @PathParam("participantId") participantId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.getProjectParticipant(projectId, participantId, extractToken(httpHeaders)) }.toJakartaResponse()

    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    @GET
    @Path("/projects/{projectName}/groups")
    @Produces(MediaType.APPLICATION_JSON)
    fun listGroups(
        @PathParam("projectName") projectName: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.listGroups(projectName, extractToken(httpHeaders)) }.toJakartaResponse()

    @POST
    @Path("/projects/{projectName}/groups")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createGroup(
        @PathParam("projectName") projectName: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.createGroup(projectName, body, extractToken(httpHeaders)) }.toJakartaResponse()

    @DELETE
    @Path("/projects/{projectName}/groups/{groupName}")
    @Produces(MediaType.APPLICATION_JSON)
    fun deleteGroup(
        @PathParam("projectName") projectName: String,
        @PathParam("groupName") groupName: String,
        @QueryParam("unlinkSubjects") unlinkSubjects: Boolean?,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking {
        apiService.deleteGroup(projectName, groupName, unlinkSubjects ?: false, extractToken(httpHeaders))
    }.toJakartaResponse()

    // ------------------------------------------------------------------ //
    //  Data sources
    // ------------------------------------------------------------------ //

    @GET
    @Path("/sources")
    @Produces(MediaType.APPLICATION_JSON)
    fun sourceCatalog(): Response =
        runBlocking { apiService.getSourceCatalog() }.toJakartaResponse()

    @GET
    @Path("/sources/projects/{projectId}/participants/{participantId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun participantSources(
        @PathParam("projectId") projectId: String,
        @PathParam("participantId") participantId: String,
    ): Response = runBlocking { apiService.getParticipantSources(projectId, participantId) }.toJakartaResponse()

    // ------------------------------------------------------------------ //
    //  Config
    // ------------------------------------------------------------------ //

    @GET
    @Path("/config/projects/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun studyConfig(
        @PathParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.getStudyConfig(projectId, extractToken(httpHeaders)) }.toJakartaResponse()

    @GET
    @Path("/config/projects/{projectId}/protocol")
    @Produces(MediaType.APPLICATION_JSON)
    fun studyProtocol(
        @PathParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.getProtocol(projectId, extractToken(httpHeaders)) }.toJakartaResponse()

    @GET
    @Path("/config/projects/{projectId}/sources/enrolment")
    @Produces(MediaType.APPLICATION_JSON)
    fun studyEnrolmentSource(
        @PathParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.getEnrolmentSource(projectId, extractToken(httpHeaders)) }.toJakartaResponse()

    @GET
    @Path("/config/projects/{projectId}/enrolment/landing")
    @Produces(MediaType.APPLICATION_JSON)
    fun studyEnrolmentLanding(
        @PathParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.getEnrolmentLanding(projectId, extractToken(httpHeaders)) }.toJakartaResponse()

    @GET
    @Path("/config/projects/{projectId}/enrolment/protocol")
    @Produces(MediaType.APPLICATION_JSON)
    fun studyEnrolmentProtocol(
        @PathParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.getEnrolmentProtocol(projectId, extractToken(httpHeaders)) }.toJakartaResponse()

    @GET
    @Path("/config/projects/{projectId}/questionnaires")
    @Produces(MediaType.APPLICATION_JSON)
    fun studyQuestionnaires(
        @PathParam("projectId") projectId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.getQuestionnaires(projectId, extractToken(httpHeaders)) }.toJakartaResponse()

    @GET
    @Path("/config/projects/{projectId}/questionnaires/{questionnaireId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun studyQuestionnaire(
        @PathParam("projectId") projectId: String,
        @PathParam("questionnaireId") questionnaireId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.getQuestionnaire(projectId, questionnaireId, extractToken(httpHeaders)) }.toJakartaResponse()

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
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Request body is required (StudyDefinition JSON)")
                .type(MediaType.APPLICATION_JSON).build()
        }
        return runBlocking { apiService.updateStudyConfig(projectId, body, extractToken(httpHeaders)) }.toJakartaResponse()
    }

    @PUT
    @Path("/config/projects/{projectId}/questionnaires")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateQuestionnaires(
        @PathParam("projectId") projectId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.updateQuestionnaires(projectId, body, extractToken(httpHeaders)) }.toJakartaResponse()

    @POST
    @Path("/config/projects/{projectId}/questionnaires")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createQuestionnaire(
        @PathParam("projectId") projectId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.createQuestionnaire(projectId, body, extractToken(httpHeaders)) }.toJakartaResponse()

    @PUT
    @Path("/config/projects/{projectId}/questionnaires/{questionnaireId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateQuestionnaire(
        @PathParam("projectId") projectId: String,
        @PathParam("questionnaireId") questionnaireId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.updateQuestionnaire(projectId, questionnaireId, body, extractToken(httpHeaders)) }.toJakartaResponse()

    @DELETE
    @Path("/config/projects/{projectId}/questionnaires/{questionnaireId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun deleteQuestionnaire(
        @PathParam("projectId") projectId: String,
        @PathParam("questionnaireId") questionnaireId: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.deleteQuestionnaire(projectId, questionnaireId, extractToken(httpHeaders)) }.toJakartaResponse()

    @PUT
    @Path("/config/projects/{projectId}/sources/protocol")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateProtocolSource(
        @PathParam("projectId") projectId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.updateProtocolSource(projectId, body, extractToken(httpHeaders)) }.toJakartaResponse()

    @PUT
    @Path("/config/projects/{projectId}/protocol")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateProtocolBody(
        @PathParam("projectId") projectId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.updateProtocolBody(projectId, body, extractToken(httpHeaders)) }.toJakartaResponse()

    @PUT
    @Path("/config/projects/{projectId}/sources/enrolment")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateEnrolmentSource(
        @PathParam("projectId") projectId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.updateEnrolmentSource(projectId, body, extractToken(httpHeaders)) }.toJakartaResponse()

    @PUT
    @Path("/config/projects/{projectId}/enrolment/landing")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateEnrolmentLandingBody(
        @PathParam("projectId") projectId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.updateEnrolmentLandingBody(projectId, body, extractToken(httpHeaders)) }.toJakartaResponse()

    @PUT
    @Path("/config/projects/{projectId}/enrolment/protocol")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateEnrolmentProtocolBody(
        @PathParam("projectId") projectId: String,
        body: String,
        @Context httpHeaders: HttpHeaders,
    ): Response = runBlocking { apiService.updateEnrolmentProtocolBody(projectId, body, extractToken(httpHeaders)) }.toJakartaResponse()


    private fun extractToken(httpHeaders: HttpHeaders): String? =
        httpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION)
            ?.firstOrNull()
            ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
            ?.substring(7)
            ?.takeIf { it.isNotBlank() }

}
