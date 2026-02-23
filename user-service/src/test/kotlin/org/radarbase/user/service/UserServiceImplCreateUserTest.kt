package org.radarbase.user.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.radarbase.user.client.KratosClient
import org.radarbase.user.client.RadarClient
import org.radarbase.user.model.KratosSubjectWebhookDTO
import org.radarbase.user.model.ManagementPortalProxyResponse
import org.radarbase.user.model.UserDTO

class UserServiceImplCreateUserTest {
    private val kratosClient: KratosClient = mockk()
    private val radarClient: RadarClient = mockk()
    private val userService = UserServiceImpl(kratosClient, radarClient)

    @Test
    fun `createUser serializes UserDTO and proxies to management portal`() =
        runBlocking {
            val projectId = "test-project"
            val request =
                UserDTO(
                    login = "researcher@test.com",
                    email = "researcher@test.com",
                    isActivated = false,
                )

            coEvery { radarClient.createUserRaw(projectId, any()) } returns
                ManagementPortalProxyResponse(status = 201, body = null)

            val result = userService.createUser(projectId, request, "Bearer token")

            assertEquals(201, result.status)

            coVerify(exactly = 1) { radarClient.createUserRaw(projectId, any()) }
            coVerify(exactly = 0) { kratosClient.createIdentity(any()) }
            coVerify(exactly = 0) { radarClient.callKratosSubjectsWebhook(any<KratosSubjectWebhookDTO>()) }
        }
}
