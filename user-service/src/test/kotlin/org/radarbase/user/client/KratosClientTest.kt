package org.radarbase.user.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.core.util.KtorClientFactory
import org.radarbase.core.util.ServiceTokenProvider
import org.radarbase.user.config.UserServiceConfig

class KratosClientTest {
    private lateinit var config: UserServiceConfig
    private lateinit var ktorClientFactory: KtorClientFactory
    private lateinit var tokenProvider: ServiceTokenProvider
    private lateinit var client: KratosClient
    private lateinit var mockEngine: MockEngine
    private lateinit var mockClient: HttpClient

    @BeforeEach
    fun setup() {
        config =
            mockk<UserServiceConfig>().apply {
                every { kratos } returns
                    UserServiceConfig.KratosConfig(
                        baseUrl = "http://kratos:4434",
                        timeoutSeconds = 5,
                        maxRetries = 3,
                    )
            }

        mockEngine =
            MockEngine { _: HttpRequestData ->
                respond(
                    content =
                    ByteReadChannel(
                        """[
                    {
                        "id": "test-id",
                        "schemaId": "default",
                        "schemaUrl": "http://kratos:4434/schemas/default",
                        "state": "active",
                        "stateChangedAt": "2024-01-01T00:00:00Z",
                        "traits": {
                            "email": "test@example.com",
                            "projects": [
                                {
                                    "id": "project-1",
                                    "name": "Test Project 1",
                                    "userId": "test-id",
                                    "consent": {
                                        "accepted": true,
                                        "timestamp": "2024-01-01T00:00:00Z"
                                    },
                                    "additional": {
                                        "role": "participant"
                                    },
                                    "eligibility": {
                                        "status": "eligible",
                                        "reason": "meets_criteria"
                                    }
                                }
                            ]
                        },
                        "verifiableAddresses": [],
                        "recoveryAddresses": [],
                        "metadataPublic": {
                            "roles": ["user"],
                            "scope": ["read"],
                            "mpLogin": "test@example.com",
                            "authorities": ["ROLE_USER"]
                        },
                        "metadataAdmin": {
                            "study": {}
                        },
                        "createdAt": "2024-01-01T00:00:00Z",
                        "updatedAt": "2024-01-01T00:00:00Z",
                        "organizationId": null
                    }
                ]""",
                    ),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

        mockClient = HttpClient(mockEngine)
        ktorClientFactory =
            mockk<KtorClientFactory>().apply {
                every { createClient(any(), any(), any()) } returns mockClient
            }

        tokenProvider =
            mockk<ServiceTokenProvider>(relaxed = true).apply {
                coEvery { getToken() } returns "test-token"
            }

        client = KratosClient(config, tokenProvider, ktorClientFactory)
    }

    @Test
    fun `test getIdentities returns list of users`() =
        runBlocking {
            val users = client.getIdentities()

            assertNotNull(users)
            assertEquals(1, users.size)
            assertEquals("test-id", users[0].id)
            assertEquals("test@example.com", users[0].traits.email)
            assertEquals(1, users[0].traits.projects.size)

            val project = users[0].traits.projects[0]
            assertEquals("project-1", project.id)
            assertEquals("Test Project 1", project.name)
            assertEquals("test-id", project.userId)
            assertNotNull(project.consent)
            assertNotNull(project.additional)
            assertNotNull(project.eligibility)
        }

    @Test
    fun `test getIdentity returns single user`() =
        runBlocking {
            val user = client.getIdentity("test-id")

            assertNotNull(user)
            assertEquals("test-id", user?.id)
            assertEquals("test@example.com", user?.traits?.email)
            assertEquals(1, user?.traits?.projects?.size)

            val project = user?.traits?.projects?.get(0)
            assertEquals("project-1", project?.id)
            assertEquals("Test Project 1", project?.name)
            assertEquals("test-id", project?.userId)
            assertNotNull(project?.consent)
            assertNotNull(project?.additional)
            assertNotNull(project?.eligibility)
        }

    @Test
    fun `test getIdentity returns null for non-existent user`() =
        runBlocking {
            mockEngine =
                MockEngine { _: HttpRequestData ->
                    respond(
                        content = ByteReadChannel(""),
                        status = HttpStatusCode.NotFound,
                    )
                }
            mockClient = HttpClient(mockEngine)
            every { ktorClientFactory.createClient(any(), any(), any()) } returns mockClient

            val testTokenProvider =
                mockk<ServiceTokenProvider>(relaxed = true).apply {
                    coEvery { getToken() } returns "test-token"
                }

            val client = KratosClient(config, testTokenProvider, ktorClientFactory)
            val user = client.getIdentity("non-existent")

            assertNull(user)
        }
}
