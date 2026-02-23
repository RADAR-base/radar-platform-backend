package org.radarbase.user.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.radarbase.user.client.KratosClient
import org.radarbase.user.client.RadarClient
import org.radarbase.user.model.TriggerRecoveryEmailRequest

class UserServiceImplTriggerRecoveryEmailTest {
    private val kratosClient: KratosClient = mockk()
    private val radarClient: RadarClient = mockk()
    private val userService = UserServiceImpl(kratosClient, radarClient)

    @Test
    fun `triggerRecoveryEmail delegates to kratos client`() =
        runBlocking {
            coEvery { kratosClient.triggerRecoveryEmail("a@b.com", "link") } returns true

            val ok = userService.triggerRecoveryEmail(TriggerRecoveryEmailRequest(email = "a@b.com"), "Bearer token")

            assertTrue(ok)
            coVerify(exactly = 1) { kratosClient.triggerRecoveryEmail("a@b.com", "link") }
        }
}
