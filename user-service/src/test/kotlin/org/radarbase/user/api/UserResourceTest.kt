package org.radarbase.user.api

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.core.Application
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.runBlocking
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.test.JerseyTest
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory
import org.glassfish.jersey.test.spi.TestContainerFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.user.config.UserServiceConfig
import org.radarbase.user.resource.UserResource
import org.radarbase.user.service.CombinedUser
import org.radarbase.user.service.UserService

class UserResourceTest : JerseyTest() {
    private lateinit var userService: UserService

    override fun getTestContainerFactory(): TestContainerFactory = GrizzlyTestContainerFactory()

    override fun configure(): Application {
        userService = mockk(relaxed = true)
        val asyncCoroutineService = mockk<AsyncCoroutineService>()

        // Stub runAsCoroutine to actually execute the block and resume the AsyncResponse,
        // otherwise the Grizzly test client will hang forever waiting for a response.
        every { asyncCoroutineService.runAsCoroutine<Any?>(any(), any(), any()) } answers {
            val asyncResponse = firstArg<AsyncResponse>()
            val block = thirdArg<suspend () -> Any?>()
            try {
                val result = runBlocking { block() }
                asyncResponse.resume(result)
            } catch (e: Exception) {
                asyncResponse.resume(e)
            }
        }

        val config = mockk<UserServiceConfig>(relaxed = true)

        return ResourceConfig(UserResource::class.java)
            .register(
                object : AbstractBinder() {
                    override fun configure() {
                        bind(userService).to(UserService::class.java)
                        bind(asyncCoroutineService).to(AsyncCoroutineService::class.java)
                        bind(config).to(UserServiceConfig::class.java)
                    }
                },
            )
    }

    @BeforeEach
    override fun setUp() {
        super.setUp()
    }

    @AfterEach
    override fun tearDown() {
        super.tearDown()
    }

    @Test
    fun `test getUsers returns OK with users list`() {
        val projectId = "test-project"
        val authToken = "Bearer test-token"
        val expectedUsers =
            listOf(
                CombinedUser(
                    id = "user1",
                    email = "user1@test.com",
                    radarMpId = "mp1",
                    radarInfo = null,
                    kratosInfo = null,
                    projectId = projectId,
                ),
            )

        coEvery { userService.getUsers(projectId, authToken) } returns expectedUsers

        val response =
            target("/user-service/project/$projectId/users")
                .request()
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .get()

        assertEquals(Response.Status.OK.statusCode, response.status)
        val users = response.readEntity(List::class.java)
        assertEquals(expectedUsers.size, users.size)
    }

    @Test
    fun `test getUser returns OK with user details`() {
        val projectId = "test-project"
        val userId = "user1"
        val authToken = "Bearer test-token"
        val expectedUser =
            CombinedUser(
                id = userId,
                email = "user1@test.com",
                radarMpId = "mp1",
                radarInfo = null,
                kratosInfo = null,
                projectId = projectId,
            )

        coEvery { userService.getUser(projectId, userId, authToken) } returns expectedUser

        val response =
            target("/user-service/project/$projectId/user/$userId")
                .request()
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .get()

        assertEquals(Response.Status.OK.statusCode, response.status)
        val user = response.readEntity(CombinedUser::class.java)
        assertEquals(userId, user.id)
    }

    @Test
    fun `test getUser returns NOT_FOUND when user doesn't exist`() {
        val projectId = "test-project"
        val userId = "non-existent"
        val authToken = "Bearer test-token"

        coEvery { userService.getUser(projectId, userId, authToken) } returns null

        val response =
            target("/user-service/project/$projectId/user/$userId")
                .request()
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .get()

        assertEquals(Response.Status.NOT_FOUND.statusCode, response.status)
    }

    @Test
    fun `test getUserByEmail returns OK with user details`() {
        val projectId = "test-project"
        val email = "user1@test.com"
        val authToken = "Bearer test-token"
        val expectedUser =
            CombinedUser(
                id = "user1",
                email = email,
                radarMpId = "mp1",
                radarInfo = null,
                kratosInfo = null,
                projectId = projectId,
            )

        coEvery { userService.getUserByEmail(projectId, email, authToken) } returns expectedUser

        val response =
            target("/user-service/project/$projectId/user/email/$email")
                .request()
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .get()

        assertEquals(Response.Status.OK.statusCode, response.status)
        val user = response.readEntity(CombinedUser::class.java)
        assertEquals(email, user.email)
    }

    @Test
    fun `test getParticipants returns OK with participants list`() {
        val projectId = "test-project"
        val authToken = "Bearer test-token"
        val expectedParticipants =
            listOf(
                CombinedUser(
                    id = "participant1",
                    email = "participant1@test.com",
                    radarMpId = "mp1",
                    radarInfo = null,
                    kratosInfo = null,
                    projectId = projectId,
                ),
            )

        coEvery { userService.getParticipants(projectId, authToken) } returns expectedParticipants

        val response =
            target("/user-service/project/$projectId/participants")
                .request()
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .get()

        assertEquals(Response.Status.OK.statusCode, response.status)
        val participants = response.readEntity(List::class.java)
        assertEquals(expectedParticipants.size, participants.size)
    }

    @Test
    fun `test getParticipant returns OK with participant details`() {
        val projectId = "test-project"
        val participantId = "participant1"
        val authToken = "Bearer test-token"
        val expectedParticipant =
            CombinedUser(
                id = participantId,
                email = "participant1@test.com",
                radarMpId = "mp1",
                radarInfo = null,
                kratosInfo = null,
                projectId = projectId,
            )

        coEvery { userService.getParticipant(projectId, participantId, authToken) } returns expectedParticipant

        val response =
            target("/user-service/project/$projectId/participants/$participantId")
                .request()
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .get()

        assertEquals(Response.Status.OK.statusCode, response.status)
        val participant = response.readEntity(CombinedUser::class.java)
        assertEquals(participantId, participant.id)
    }

    @Test
    fun `test getParticipant returns NOT_FOUND when participant doesn't exist`() {
        val projectId = "test-project"
        val participantId = "non-existent"
        val authToken = "Bearer test-token"

        coEvery { userService.getParticipant(projectId, participantId, authToken) } returns null

        val response =
            target("/user-service/project/$projectId/participants/$participantId")
                .request()
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .get()

        assertEquals(Response.Status.NOT_FOUND.statusCode, response.status)
    }

    @Test
    fun `test endpoints return UNAUTHORIZED when auth token is missing`() {
        val projectId = "test-project"
        val userId = "user1"
        val participantId = "participant1"

        // Test getUsers
        var response =
            target("/user-service/project/$projectId/users")
                .request()
                .get()
        assertEquals(Response.Status.OK.statusCode, response.status)

        // Test getUser
        response =
            target("/user-service/project/$projectId/user/$userId")
                .request()
                .get()
        assertEquals(Response.Status.OK.statusCode, response.status)

        // Test getParticipants
        response =
            target("/user-service/project/$projectId/participants")
                .request()
                .get()
        assertEquals(Response.Status.UNAUTHORIZED.statusCode, response.status)

        // Test getParticipant
        response =
            target("/user-service/project/$projectId/participants/$participantId")
                .request()
                .get()
        assertEquals(Response.Status.UNAUTHORIZED.statusCode, response.status)
    }
}
