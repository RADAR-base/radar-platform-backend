package org.radarbase.user.resource

import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
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
import org.radarbase.user.config.UserServiceConfig
import org.radarbase.user.model.CreateParticipantRequest
import org.radarbase.user.model.Participant
import org.radarbase.user.model.TriggerRecoveryEmailRequest
import org.radarbase.user.model.UpdateUserRequest
import org.radarbase.user.model.UserDTO
import org.radarbase.user.service.CombinedUser
import org.radarbase.user.service.UserService
import org.slf4j.LoggerFactory
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

@Path("/user-service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
class UserResource
    @Inject
    constructor(
        private val userService: UserService,
        private val asyncCoroutineService: AsyncCoroutineService,
        private val config: UserServiceConfig,
    ) {
        private val logger = LoggerFactory.getLogger(UserResource::class.java)
        private val timeout =
            config.server.requestTimeout.seconds
                .toDuration(SECONDS)

        @GET
        @Path("/project/{projectId}/users")
        fun getUsers(
            @PathParam("projectId") projectId: String,
            @Context headers: HttpHeaders,
            @Suspended asyncResponse: AsyncResponse,
        ) {
            try {
                asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                    userService
                        .getUsers(projectId, headers.getHeaderString(HttpHeaders.AUTHORIZATION)!!)
                        .map { it.toParticipant() }
                }
            } catch (e: Exception) {
                logger.error("Error fetching users for project $projectId", e)
                asyncResponse.resume(
                    Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error fetching users: ${e.message}")
                        .build(),
                )
            }
        }

        @POST
        @Path("/project/{projectId}/users")
        fun createUser(
            @PathParam("projectId") projectId: String,
            @Context headers: HttpHeaders,
            request: UserDTO,
            @Suspended asyncResponse: AsyncResponse,
        ) {
            try {
                asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                    val mpResponse =
                        userService.createUser(
                            projectId,
                            request,
                            headers.getHeaderString(HttpHeaders.AUTHORIZATION)!!,
                        )
                    Response
                        .status(mpResponse.status)
                        .entity(mpResponse.body)
                        .build()
                }
            } catch (e: IllegalArgumentException) {
                asyncResponse.resume(
                    Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(e.message ?: "Invalid request")
                        .build(),
                )
            } catch (e: Exception) {
                logger.error("Error creating user for project $projectId", e)
                asyncResponse.resume(
                    Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error creating user: ${e.message}")
                        .build(),
                )
            }
        }

        @GET
        @Path("/project/{projectId}/user/{userId}")
        fun getUser(
            @PathParam("projectId") projectId: String,
            @PathParam("userId") userId: String,
            @Context headers: HttpHeaders,
            @Suspended asyncResponse: AsyncResponse,
        ) {
            try {
                asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                    val user =
                        userService
                            .getUser(projectId, userId, headers.getHeaderString(HttpHeaders.AUTHORIZATION)!!)
                            ?.toParticipant()
                    if (user == null) {
                        Response
                            .status(Response.Status.NOT_FOUND)
                            .entity("User $userId not found")
                            .build()
                    } else {
                        Response.ok(user).build()
                    }
                }
            } catch (e: Exception) {
                logger.error("Error fetching user $userId for project $projectId", e)
                asyncResponse.resume(
                    Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error fetching user: ${e.message}")
                        .build(),
                )
            }
        }

        @GET
        @Path("/project/{projectId}/user/email/{email}")
        fun getUserByEmail(
            @PathParam("projectId") projectId: String,
            @PathParam("email") email: String,
            @Context headers: HttpHeaders,
            @Suspended asyncResponse: AsyncResponse,
        ) {
            try {
                asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                    val user =
                        userService.getUserByEmail(
                            projectId,
                            email,
                            headers.getHeaderString(HttpHeaders.AUTHORIZATION)!!,
                        )
                    if (user == null) {
                        Response
                            .status(Response.Status.NOT_FOUND)
                            .entity("User with email $email not found")
                            .build()
                    } else {
                        Response.ok(user).build()
                    }
                }
            } catch (e: Exception) {
                logger.error("Error fetching user by email $email for project $projectId", e)
                asyncResponse.resume(
                    Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error fetching user: ${e.message}")
                        .build(),
                )
            }
        }

        @POST
        @Path("/recovery")
        fun triggerRecoveryEmail(
            @Context headers: HttpHeaders,
            request: TriggerRecoveryEmailRequest,
            @Suspended asyncResponse: AsyncResponse,
        ) {
            try {
                asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                    val ok =
                        userService.triggerRecoveryEmail(
                            request,
                            headers.getHeaderString(HttpHeaders.AUTHORIZATION)!!,
                        )
                    if (ok) {
                        Response.accepted(mapOf("status" to "sent")).build()
                    } else {
                        Response
                            .status(Response.Status.BAD_GATEWAY)
                            .entity("Failed to trigger recovery email")
                            .build()
                    }
                }
            } catch (e: Exception) {
                logger.error("Error triggering recovery email", e)
                asyncResponse.resume(
                    Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error triggering recovery email: ${e.message}")
                        .build(),
                )
            }
        }

        @GET
        @Path("/project/{projectId}/participants")
        fun getParticipants(
            @PathParam("projectId") projectId: String,
            @Context headers: HttpHeaders,
            @Suspended asyncResponse: AsyncResponse,
        ) {
            try {
                asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                    userService
                        .getParticipants(projectId, headers.getHeaderString(HttpHeaders.AUTHORIZATION)!!)
                        .map { it.toParticipant() }
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

        @POST
        @Path("/project/{projectId}/participants")
        fun createParticipant(
            @PathParam("projectId") projectId: String,
            @Context headers: HttpHeaders,
            request: CreateParticipantRequest,
            @Suspended asyncResponse: AsyncResponse,
        ) {
            try {
                asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                    val created =
                        userService.createParticipant(
                            projectId,
                            request,
                            headers.getHeaderString(HttpHeaders.AUTHORIZATION)!!,
                        )
                    if (created == null) {
                        Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Failed to create participant")
                            .build()
                    } else {
                        Response.ok(created.toParticipant()).build()
                    }
                }
            } catch (e: Exception) {
                logger.error("Error creating participant for project $projectId", e)
                asyncResponse.resume(
                    Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error creating participant: ${e.message}")
                        .build(),
                )
            }
        }

        @GET
        @Path("/project/{projectId}/participants/{participantId}")
        fun getParticipant(
            @PathParam("projectId") projectId: String,
            @PathParam("participantId") participantId: String,
            @Context headers: HttpHeaders,
            @Suspended asyncResponse: AsyncResponse,
        ) {
            try {
                asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                    val participant =
                        userService
                            .getParticipant(
                                projectId,
                                participantId,
                                headers.getHeaderString(HttpHeaders.AUTHORIZATION)!!,
                            )?.toParticipant()
                    if (participant == null) {
                        Response
                            .status(Response.Status.NOT_FOUND)
                            .entity("Participant $participantId not found")
                            .build()
                    } else {
                        Response.ok(participant).build()
                    }
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

        @PUT
        @Path("/project/{projectId}/user/{userId}")
        fun updateUser(
            @PathParam("projectId") projectId: String,
            @PathParam("userId") userId: String,
            @Context headers: HttpHeaders,
            updateRequest: UpdateUserRequest,
            @Suspended asyncResponse: AsyncResponse,
        ) {
            try {
                asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                    val updatedUser =
                        userService.updateUser(
                            projectId,
                            userId,
                            updateRequest,
                            headers.getHeaderString(HttpHeaders.AUTHORIZATION)!!,
                        )
                    if (updatedUser == null) {
                        Response
                            .status(Response.Status.NOT_FOUND)
                            .entity("User $userId not found or update failed")
                            .build()
                    } else {
                        Response.ok(updatedUser.toParticipant()).build()
                    }
                }
            } catch (e: Exception) {
                logger.error("Error updating user $userId for project $projectId", e)
                asyncResponse.resume(
                    Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error updating user: ${e.message}")
                        .build(),
                )
            }
        }

        @PUT
        @Path("/project/{projectId}/participants/{participantId}")
        fun updateParticipant(
            @PathParam("projectId") projectId: String,
            @PathParam("participantId") participantId: String,
            @Context headers: HttpHeaders,
            updateRequest: UpdateUserRequest,
            @Suspended asyncResponse: AsyncResponse,
        ) {
            try {
                asyncCoroutineService.runAsCoroutine(asyncResponse, timeout) {
                    val updatedParticipant =
                        userService.updateParticipant(
                            projectId,
                            participantId,
                            updateRequest,
                            headers.getHeaderString(HttpHeaders.AUTHORIZATION)!!,
                        )
                    if (updatedParticipant == null) {
                        Response
                            .status(Response.Status.NOT_FOUND)
                            .entity("Participant $participantId not found or update failed")
                            .build()
                    } else {
                        Response.ok(updatedParticipant.toParticipant()).build()
                    }
                }
            } catch (e: Exception) {
                logger.error("Error updating participant $participantId for project $projectId", e)
                asyncResponse.resume(
                    Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error updating participant: ${e.message}")
                        .build(),
                )
            }
        }

        fun CombinedUser.toParticipant() =
            Participant(
                id = id,
                projectId = projectId,
                name = email,
                email = email,
                status = radarInfo?.status ?: kratosInfo?.state ?: "unknown",
                sources = radarInfo?.sources ?: emptyList(),
                studyGroup = radarInfo?.group ?: "",
                enrollmentDate = radarInfo?.enrollmentDate ?: "",
                attributes = radarInfo?.attributes ?: emptyMap(),
                dateOfBirth = radarInfo?.dateOfBirth,
                personName = radarInfo?.personName,
                radarInfo = radarInfo,
                kratosInfo = kratosInfo,
                mpLogin = kratosInfo?.metadataPublic?.mpLogin,
            )
    }
