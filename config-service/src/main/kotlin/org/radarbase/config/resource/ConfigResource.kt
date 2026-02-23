package org.radarbase.config.resource

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.radarbase.config.model.CreateQuestionnaireRequest
import org.radarbase.config.model.Questionnaire
import org.radarbase.config.model.SourceRef
import org.radarbase.config.model.StudyDefinition
import org.radarbase.config.model.UpdateQuestionnaireRequest
import org.radarbase.config.service.StudyConfigService

@Path("/config-service")
@Singleton
class ConfigResource
    @Inject
    constructor(
        private val studyConfigService: StudyConfigService,
    ) {
        private val httpClient: HttpClient = HttpClient(CIO)

        private val json =
            Json {
                ignoreUnknownKeys = true
            }

        @GET
        @Path("/projects/{projectName}")
        @Produces(MediaType.APPLICATION_JSON)
        fun getStudyConfig(
            @PathParam("projectName") projectName: String,
        ): Response =
            runBlocking {
                val config = studyConfigService.getStudyConfig(projectName)
                if (config == null) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Study config not found for project $projectName")
                        .build()
                } else {
                    Response.ok(config).build()
                }
            }

        @GET
        @Path("/projects/{projectName}/metadata")
        @Produces(MediaType.APPLICATION_JSON)
        fun getMetadata(
            @PathParam("projectName") projectName: String,
        ): Response =
            runBlocking {
                val def = studyConfigService.getStudyDefinition(projectName)
                if (def == null) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Study config not found for project $projectName")
                        .build()
                } else {
                    Response.ok(def.metadata).build()
                }
            }

        /**
         * Reusable helper to fetch JSON from a remote URL.
         */
        private suspend fun fetchJson(url: String): String =
            httpClient
                .get(url) {
                    header(HttpHeaders.Accept, "application/json")
                }.bodyAsText()

        /**
         * Converts a GitHub tree URL to a raw file URL and appends the filename.
         * Handles both GitHub tree URLs and regular URLs.
         *
         * @param href The source URL (may be a GitHub tree URL or regular URL)
         * @param filename The filename to append (e.g., "protocol.json")
         * @return The converted URL pointing to the raw file
         */
        private fun toRawFileUrl(
            href: String,
            filename: String,
        ): String {
            val baseUrl =
                if (href.contains("github.com") && href.contains("/tree/")) {
                    // Convert GitHub tree URL to raw GitHub URL
                    // e.g., https://github.com/org/repo/tree/branch/path -> https://raw.githubusercontent.com/org/repo/branch/path
                    href
                        .replace("github.com", "raw.githubusercontent.com")
                        .replace("/tree/", "/")
                        .trimEnd('/')
                } else {
                    href.trimEnd('/')
                }
            return "$baseUrl/$filename"
        }

        @GET
        @Path("/projects/{projectName}/population")
        @Produces(MediaType.APPLICATION_JSON)
        fun getPopulation(
            @PathParam("projectName") projectName: String,
        ): Response =
            runBlocking {
                val def = studyConfigService.getStudyDefinition(projectName)
                if (def == null) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Study config not found for project $projectName")
                        .build()
                } else {
                    Response.ok(def.population).build()
                }
            }

        @GET
        @Path("/projects/{projectName}/domain")
        @Produces(MediaType.APPLICATION_JSON)
        fun getDomain(
            @PathParam("projectName") projectName: String,
        ): Response =
            runBlocking {
                val def = studyConfigService.getStudyDefinition(projectName)
                if (def == null) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Study config not found for project $projectName")
                        .build()
                } else {
                    Response.ok(def.domain).build()
                }
            }

        @GET
        @Path("/projects/{projectName}/eligibility")
        @Produces(MediaType.APPLICATION_JSON)
        fun getEligibility(
            @PathParam("projectName") projectName: String,
        ): Response =
            runBlocking {
                val def = studyConfigService.getStudyDefinition(projectName)
                if (def == null) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Study config not found for project $projectName")
                        .build()
                } else {
                    Response.ok(def.eligibility).build()
                }
            }

        @GET
        @Path("/projects/{projectName}/design")
        @Produces(MediaType.APPLICATION_JSON)
        fun getDesign(
            @PathParam("projectName") projectName: String,
        ): Response =
            runBlocking {
                val def = studyConfigService.getStudyDefinition(projectName)
                if (def == null) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Study config not found for project $projectName")
                        .build()
                } else {
                    Response.ok(def.design).build()
                }
            }

        @GET
        @Path("/projects/{projectName}/technology")
        @Produces(MediaType.APPLICATION_JSON)
        fun getTechnology(
            @PathParam("projectName") projectName: String,
        ): Response =
            runBlocking {
                val def = studyConfigService.getStudyDefinition(projectName)
                if (def == null) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Study config not found for project $projectName")
                        .build()
                } else {
                    Response.ok(def.technology).build()
                }
            }

        @GET
        @Path("/projects/{projectName}/contact")
        @Produces(MediaType.APPLICATION_JSON)
        fun getContact(
            @PathParam("projectName") projectName: String,
        ): Response =
            runBlocking {
                val def = studyConfigService.getStudyDefinition(projectName)
                if (def == null) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Study config not found for project $projectName")
                        .build()
                } else {
                    Response.ok(def.contact).build()
                }
            }

        @GET
        @Path("/projects/{projectName}/sources")
        @Produces(MediaType.APPLICATION_JSON)
        fun getSources(
            @PathParam("projectName") projectName: String,
        ): Response =
            runBlocking {
                val def = studyConfigService.getStudyDefinition(projectName)
                if (def == null) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Study config not found for project $projectName")
                        .build()
                } else {
                    Response.ok(def.sources).build()
                }
            }

        @GET
        @Path("/projects/{projectName}/sources/enrolment")
        @Produces(MediaType.APPLICATION_JSON)
        fun getEnrolmentSource(
            @PathParam("projectName") projectName: String,
        ): Response =
            runBlocking {
                val landingOverride = studyConfigService.getEnrolmentLandingBodyOverride(projectName)
                val protocolOverride = studyConfigService.getEnrolmentProtocolBodyOverride(projectName)
                if (landingOverride != null && protocolOverride != null) {
                    val landingpageElement = json.parseToJsonElement(landingOverride)
                    val protocolElement = json.parseToJsonElement(protocolOverride)
                    val result =
                        buildJsonObject {
                            put("landingpage", landingpageElement)
                            put("protocol", protocolElement)
                        }
                    return@runBlocking Response.ok(result.toString(), MediaType.APPLICATION_JSON).build()
                }

                val def = studyConfigService.getStudyDefinition(projectName)
                if (def == null) {
                    return@runBlocking Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Study config not found for project $projectName")
                        .build()
                }

                val landingpageUrl = toRawFileUrl(def.sources.enrolment.href, "landingpage.json")
                val protocolUrl = toRawFileUrl(def.sources.enrolment.href, "protocol.json")

                try {
                    val landingpageBody = landingOverride ?: fetchJson(landingpageUrl)
                    val protocolBody = protocolOverride ?: fetchJson(protocolUrl)
                    val landingpageElement = json.parseToJsonElement(landingpageBody)
                    val protocolElement = json.parseToJsonElement(protocolBody)
                    val result =
                        buildJsonObject {
                            put("landingpage", landingpageElement)
                            put("protocol", protocolElement)
                        }
                    Response
                        .ok(result.toString(), MediaType.APPLICATION_JSON)
                        .build()
                } catch (ex: Exception) {
                    Response
                        .status(Response.Status.BAD_GATEWAY)
                        .entity("Failed to fetch enrolment source from ${def.sources.enrolment.href}: ${ex.message}")
                        .build()
                }
            }

        @GET
        @Path("/projects/{projectName}/enrolment/landing")
        @Produces(MediaType.APPLICATION_JSON)
        fun getEnrolmentLanding(
            @PathParam("projectName") projectName: String,
        ): Response =
            runBlocking {
                val override = studyConfigService.getEnrolmentLandingBodyOverride(projectName)
                if (override != null) {
                    return@runBlocking Response.ok(override, MediaType.APPLICATION_JSON).build()
                }

                val def = studyConfigService.getStudyDefinition(projectName)
                if (def == null) {
                    return@runBlocking Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Study config not found for project $projectName")
                        .build()
                }

                val url = toRawFileUrl(def.sources.enrolment.href, "landingpage.json")
                try {
                    val body = fetchJson(url)
                    Response.ok(body, MediaType.APPLICATION_JSON).build()
                } catch (ex: Exception) {
                    Response
                        .status(Response.Status.BAD_GATEWAY)
                        .entity("Failed to fetch enrolment landing from $url: ${ex.message}")
                        .build()
                }
            }

        @GET
        @Path("/projects/{projectName}/enrolment/protocol")
        @Produces(MediaType.APPLICATION_JSON)
        fun getEnrolmentProtocol(
            @PathParam("projectName") projectName: String,
        ): Response =
            runBlocking {
                val override = studyConfigService.getEnrolmentProtocolBodyOverride(projectName)
                if (override != null) {
                    return@runBlocking Response.ok(override, MediaType.APPLICATION_JSON).build()
                }

                val def = studyConfigService.getStudyDefinition(projectName)
                if (def == null) {
                    return@runBlocking Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Study config not found for project $projectName")
                        .build()
                }

                val url = toRawFileUrl(def.sources.enrolment.href, "protocol.json")
                try {
                    val body = fetchJson(url)
                    Response.ok(body, MediaType.APPLICATION_JSON).build()
                } catch (ex: Exception) {
                    Response
                        .status(Response.Status.BAD_GATEWAY)
                        .entity("Failed to fetch enrolment protocol from $url: ${ex.message}")
                        .build()
                }
            }

        @GET
        @Path("/projects/{projectName}/sources/protocol")
        @Produces(MediaType.APPLICATION_JSON)
        fun getProtocolSource(
            @PathParam("projectName") projectName: String,
        ): Response =
            runBlocking {
                val override = studyConfigService.getProtocolBodyOverride(projectName)
                if (override != null) {
                    return@runBlocking Response.ok(override, MediaType.APPLICATION_JSON).build()
                }

                val def = studyConfigService.getStudyDefinition(projectName)
                if (def == null) {
                    return@runBlocking Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Study config not found for project $projectName")
                        .build()
                }

                val url = toRawFileUrl(def.sources.protocol.href, "protocol.json")

                try {
                    val body = fetchJson(url)
                    Response
                        .ok(body, MediaType.APPLICATION_JSON)
                        .build()
                } catch (ex: Exception) {
                    Response
                        .status(Response.Status.BAD_GATEWAY)
                        .entity("Failed to fetch protocol body from $url: ${ex.message}")
                        .build()
                }
            }

        @GET
        @Path("/projects/{projectName}/questionnaires")
        @Produces(MediaType.APPLICATION_JSON)
        fun getQuestionnaires(
            @PathParam("projectName") projectName: String,
        ): Response =
            runBlocking {
                val def = studyConfigService.getStudyDefinition(projectName)
                if (def == null) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Study config not found for project $projectName")
                        .build()
                } else {
                    Response.ok(def.questionnaires).build()
                }
            }

        @GET
        @Path("/projects/{projectName}/questionnaires/{questionnaireId}")
        @Produces(MediaType.APPLICATION_JSON)
        fun getQuestionnaire(
            @PathParam("projectName") projectName: String,
            @PathParam("questionnaireId") questionnaireId: String,
        ): Response =
            runBlocking {
                val override = studyConfigService.getQuestionnaireBodyOverride(projectName, questionnaireId)
                if (override != null) {
                    return@runBlocking Response.ok(override, MediaType.APPLICATION_JSON).build()
                }

                val def = studyConfigService.getStudyDefinition(projectName)
                if (def == null) {
                    return@runBlocking Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Study config not found for project $projectName")
                        .build()
                }

                val q = def.questionnaires.find { it.id == questionnaireId }
                if (q == null) {
                    return@runBlocking Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Questionnaire $questionnaireId not found for project $projectName")
                        .build()
                }

                // Parse href which may be in format: "baseUrl|/path/to/file.json"
                // If pipe separator exists, use the part after pipe as the path
                // Otherwise, use the href directly as the URL
                val url =
                    if (q.href.contains("|")) {
                        val parts = q.href.split("|", limit = 2)
                        val baseUrl = parts[0].trimEnd('/')
                        val filePath = parts[1].trimStart('/')
                        "$baseUrl/$filePath"
                    } else {
                        q.href
                    }

                try {
                    val body = fetchJson(url)
                    Response
                        .ok(body, MediaType.APPLICATION_JSON)
                        .build()
                } catch (ex: Exception) {
                    Response
                        .status(Response.Status.BAD_GATEWAY)
                        .entity("Failed to fetch questionnaire body from $url: ${ex.message}")
                        .build()
                }
            }

        @PUT
        @Path("/projects/{projectName}")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        fun updateStudyConfig(
            @PathParam("projectName") projectName: String,
            body: String?,
        ): Response =
            runBlocking {
                if (body.isNullOrBlank()) {
                    return@runBlocking Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Request body is required and must be valid StudyDefinition JSON")
                        .build()
                }
                try {
                    val definition = json.decodeFromString<StudyDefinition>(body)
                    studyConfigService.updateStudyConfig(projectName, definition)
                    Response.ok(definition).build()
                } catch (ex: kotlinx.serialization.SerializationException) {
                    Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Invalid study config JSON: ${ex.message}")
                        .build()
                }
            }

        @PUT
        @Path("/projects/{projectName}/questionnaires")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        fun updateQuestionnaires(
            @PathParam("projectName") projectName: String,
            body: String,
        ): Response =
            runBlocking {
                try {
                    val questionnaires = json.decodeFromString<List<Questionnaire>>(body)
                    studyConfigService.updateQuestionnaires(projectName, questionnaires)
                    Response.ok(questionnaires).build()
                } catch (ex: NoSuchElementException) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(ex.message)
                        .build()
                } catch (ex: kotlinx.serialization.SerializationException) {
                    Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Invalid questionnaires JSON: ${ex.message}")
                        .build()
                }
            }

        @POST
        @Path("/projects/{projectName}/questionnaires")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        fun createQuestionnaire(
            @PathParam("projectName") projectName: String,
            body: String,
        ): Response =
            runBlocking {
                try {
                    val request = json.decodeFromString<CreateQuestionnaireRequest>(body)
                    studyConfigService.createQuestionnaire(projectName, request)
                    Response
                        .status(Response.Status.CREATED)
                        .entity(request.questionnaire)
                        .build()
                } catch (ex: NoSuchElementException) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(ex.message)
                        .build()
                } catch (ex: IllegalArgumentException) {
                    Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(ex.message)
                        .build()
                } catch (ex: kotlinx.serialization.SerializationException) {
                    Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Invalid request JSON: ${ex.message}")
                        .build()
                }
            }

        @DELETE
        @Path("/projects/{projectName}/questionnaires/{questionnaireId}")
        @Produces(MediaType.APPLICATION_JSON)
        fun deleteQuestionnaire(
            @PathParam("projectName") projectName: String,
            @PathParam("questionnaireId") questionnaireId: String,
        ): Response =
            runBlocking {
                try {
                    studyConfigService.deleteQuestionnaire(projectName, questionnaireId)
                    Response.noContent().build()
                } catch (ex: NoSuchElementException) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(ex.message)
                        .build()
                }
            }

        @PUT
        @Path("/projects/{projectName}/questionnaires/{questionnaireId}")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        fun updateQuestionnaire(
            @PathParam("projectName") projectName: String,
            @PathParam("questionnaireId") questionnaireId: String,
            body: String,
        ): Response =
            runBlocking {
                try {
                    val request = json.decodeFromString<UpdateQuestionnaireRequest>(body)
                    val questionnaire = request.questionnaire
                    if (questionnaire.id != questionnaireId) {
                        return@runBlocking Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity("Questionnaire id in path ($questionnaireId) does not match body (${questionnaire.id})")
                            .build()
                    }
                    studyConfigService.updateQuestionnaire(projectName, questionnaireId, questionnaire, request.body)
                    Response.ok(questionnaire).build()
                } catch (ex: NoSuchElementException) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(ex.message)
                        .build()
                } catch (ex: kotlinx.serialization.SerializationException) {
                    Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Invalid questionnaire JSON: ${ex.message}")
                        .build()
                }
            }

        @PUT
        @Path("/projects/{projectName}/sources/protocol")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        fun updateProtocolSource(
            @PathParam("projectName") projectName: String,
            body: String,
        ): Response =
            runBlocking {
                try {
                    val protocol = json.decodeFromString<SourceRef>(body)
                    studyConfigService.updateProtocolSource(projectName, protocol)
                    Response.ok(protocol).build()
                } catch (ex: NoSuchElementException) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(ex.message)
                        .build()
                } catch (ex: kotlinx.serialization.SerializationException) {
                    Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Invalid protocol source JSON: ${ex.message}")
                        .build()
                }
            }

        @PUT
        @Path("/projects/{projectName}/sources/enrolment")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        fun updateEnrolmentSource(
            @PathParam("projectName") projectName: String,
            body: String,
        ): Response =
            runBlocking {
                try {
                    val enrolment = json.decodeFromString<SourceRef>(body)
                    studyConfigService.updateEnrolmentSource(projectName, enrolment)
                    Response.ok(enrolment).build()
                } catch (ex: NoSuchElementException) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(ex.message)
                        .build()
                } catch (ex: kotlinx.serialization.SerializationException) {
                    Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Invalid enrolment source JSON: ${ex.message}")
                        .build()
                }
            }

        @PUT
        @Path("/projects/{projectName}/enrolment/landing")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        fun updateEnrolmentLandingBody(
            @PathParam("projectName") projectName: String,
            body: String,
        ): Response =
            runBlocking {
                try {
                    studyConfigService.updateEnrolmentLandingBody(projectName, body)
                    Response.ok(body, MediaType.APPLICATION_JSON).build()
                } catch (ex: NoSuchElementException) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(ex.message)
                        .build()
                } catch (ex: IllegalStateException) {
                    Response
                        .status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(ex.message)
                        .build()
                }
            }

        @PUT
        @Path("/projects/{projectName}/enrolment/protocol")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        fun updateEnrolmentProtocolBody(
            @PathParam("projectName") projectName: String,
            body: String,
        ): Response =
            runBlocking {
                try {
                    studyConfigService.updateEnrolmentProtocolBody(projectName, body)
                    Response.ok(body, MediaType.APPLICATION_JSON).build()
                } catch (ex: NoSuchElementException) {
                    Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(ex.message)
                        .build()
                } catch (ex: IllegalStateException) {
                    Response
                        .status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(ex.message)
                        .build()
                }
            }

        @PUT
        @Path("/projects/{projectName}/protocol")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        fun updateProtocolBody(
            @PathParam("projectName") projectName: String,
            body: String,
        ): Response =
            runBlocking {
                studyConfigService.updateProtocolBody(projectName, body)
                Response.ok(body, MediaType.APPLICATION_JSON).build()
            }
    }
