package org.radarbase.user.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.radarbase.user.client.KratosClient
import org.radarbase.user.client.RadarClient
import org.radarbase.user.model.CreateParticipantRequest
import org.radarbase.user.model.KratosCreateIdentityRequest
import org.radarbase.user.model.KratosSubjectWebhookDTO
import org.radarbase.user.model.KratosTraits
import org.radarbase.user.model.KratosUser

class UserServiceImplCreateParticipantTest {
    private val kratosClient: KratosClient = mockk()
    private val radarClient: RadarClient = mockk()
    private val userService = UserServiceImpl(kratosClient, radarClient)

    @Test
    fun `createParticipant creates kratos identity then calls webhook`() =
        runBlocking {
            val projectId = "test-project"
            val request =
                CreateParticipantRequest(
                    email = "participant@test.com",
                    projectUserId = "subject-login-123",
                    state = "active",
                )

            val createdIdentity =
                KratosUser(
                    id = "kratos-id-1",
                    schemaId = "subject",
                    schemaUrl = "http://kratos/schemas/subject",
                    state = "active",
                    stateChangedAt = null,
                    traits = KratosTraits(email = request.email),
                    verifiableAddresses = emptyList(),
                    recoveryAddresses = emptyList(),
                    metadataPublic = null,
                    metadataAdmin = null,
                    createdAt = "2024-01-01T00:00:00Z",
                    updatedAt = "2024-01-01T00:00:00Z",
                    organizationId = null,
                )

            val capturedCreateReq = slot<KratosCreateIdentityRequest>()
            coEvery { kratosClient.createIdentity(capture(capturedCreateReq)) } returns createdIdentity
            coEvery { radarClient.callKratosSubjectsWebhook(any()) } returns true
            coEvery { radarClient.getParticipant(projectId, createdIdentity.id) } returns null

            val result = userService.createParticipant(projectId, request, "Bearer token")

            assertNotNull(result)
            assertEquals(createdIdentity.id, result!!.id)
            assertEquals(projectId, result.projectId)
            assertEquals(request.email, result.email)

            assertEquals("subject", capturedCreateReq.captured.schemaId)
            assertEquals(request.email, capturedCreateReq.captured.traits.email)
            assertEquals(request.state, capturedCreateReq.captured.state)
            assertEquals(null, capturedCreateReq.captured.metadataPublic)
            assertEquals(
                projectId,
                capturedCreateReq.captured.traits.projects
                    .first()
                    .id,
            )
            assertEquals(
                request.projectUserId,
                capturedCreateReq.captured.traits.projects
                    .first()
                    .userId,
            )

            coVerify(exactly = 1) { radarClient.callKratosSubjectsWebhook(any<KratosSubjectWebhookDTO>()) }
        }
}
