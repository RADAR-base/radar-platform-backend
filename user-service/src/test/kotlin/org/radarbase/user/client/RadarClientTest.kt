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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.core.util.KtorClientFactory
import org.radarbase.core.util.ServiceTokenProvider
import org.radarbase.user.config.UserServiceConfig

class RadarClientTest {
    private lateinit var config: UserServiceConfig
    private lateinit var ktorClientFactory: KtorClientFactory
    private lateinit var tokenProvider: ServiceTokenProvider
    private lateinit var client: RadarClient
    private lateinit var mockEngine: MockEngine
    private lateinit var mockClient: HttpClient

    @BeforeEach
    fun setup() {
        config =
            mockk<UserServiceConfig>().apply {
                every { managementPortal } returns
                    UserServiceConfig.ManagementPortalConfig(
                        baseUrl = "http://management-portal:8080/api",
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
                        "id": 1,
                        "externalLink": "https://example.com/user/1",
                        "externalId": "ext-123",
                        "status": "ACTIVE",
                        "group": "test-group",
                        "dateOfBirth": "1990-01-01",
                        "enrollmentDate": "2024-01-01",
                        "personName": "Test User",
                        "roles": [
                            {
                                "id": 1,
                                "projectId": 1,
                                "projectName": "Test Project",
                                "authorityName": "RESEARCHER"
                            }
                        ],
                        "sources": [
                            {
                                "id": 1,
                                "sourceTypeId": 1,
                                "sourceTypeProducer": "Test Producer",
                                "sourceTypeModel": "Test Model",
                                "sourceTypeCatalogVersion": "1.0",
                                "expectedSourceName": "Test Source",
                                "sourceId": "src-123",
                                "sourceName": "Test Source Name",
                                "attributes": {
                                    "key1": "value1"
                                },
                                "assigned": true
                            }
                        ],
                        "attributes": {
                            "key1": "value1",
                            "key2": "value2"
                        },
                        "login": "testuser",
                        "identity": "test-identity"
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

        client = RadarClient(config, tokenProvider, ktorClientFactory)
    }

    @Test
    fun `test getUsers returns list of users`() =
        runBlocking {
            val users = client.getUsers("project-1")

            assertNotNull(users)
            assertEquals(1, users.size)

            val user = users[0]
            assertEquals(1L, user.id)
            assertEquals("https://example.com/user/1", user.externalLink)
            assertEquals("ext-123", user.externalId)
            assertEquals("ACTIVE", user.status)
            assertEquals("test-group", user.group)
            assertEquals("1990-01-01", user.dateOfBirth)
            assertEquals("2024-01-01", user.enrollmentDate)
            assertEquals("Test User", user.personName)
            assertEquals("testuser", user.login)
            assertEquals("test-identity", user.identity)

            // Test roles
            assertEquals(1, user.roles.size)
            val role = user.roles[0]
            assertEquals(1L, role.id)
            assertEquals(1L, role.projectId)
            assertEquals("Test Project", role.projectName)
            assertEquals("RESEARCHER", role.authorityName)

            // Test sources
            assertEquals(1, user.sources.size)
            val source = user.sources[0]
            assertEquals(1L, source.id)
            assertEquals(1L, source.sourceTypeId)
            assertEquals("Test Producer", source.sourceTypeProducer)
            assertEquals("Test Model", source.sourceTypeModel)
            assertEquals("1.0", source.sourceTypeCatalogVersion)
            assertEquals("Test Source", source.expectedSourceName)
            assertEquals("src-123", source.sourceId)
            assertEquals("Test Source Name", source.sourceName)
            assertEquals(mapOf("key1" to "value1"), source.attributes)
            assertTrue(source.assigned)

            // Test attributes
            assertEquals(mapOf("key1" to "value1", "key2" to "value2"), user.attributes)
        }

    @Test
    fun `test getUser returns single user`() =
        runBlocking {
            val user = client.getUser("project-1", "1")

            assertNotNull(user)
            assertEquals(1L, user?.id)
            assertEquals("https://example.com/user/1", user?.externalLink)
            assertEquals("ext-123", user?.externalId)
            assertEquals("ACTIVE", user?.status)
            assertEquals("test-group", user?.group)
            assertEquals("1990-01-01", user?.dateOfBirth)
            assertEquals("2024-01-01", user?.enrollmentDate)
            assertEquals("Test User", user?.personName)
            assertEquals("testuser", user?.login)
            assertEquals("test-identity", user?.identity)

            // Test roles
            assertEquals(1, user?.roles?.size)
            val role = user?.roles?.get(0)
            assertEquals(1L, role?.id)
            assertEquals(1L, role?.projectId)
            assertEquals("Test Project", role?.projectName)
            assertEquals("RESEARCHER", role?.authorityName)

            // Test sources
            assertEquals(1, user?.sources?.size)
            val source = user?.sources?.get(0)
            assertEquals(1L, source?.id)
            assertEquals(1L, source?.sourceTypeId)
            assertEquals("Test Producer", source?.sourceTypeProducer)
            assertEquals("Test Model", source?.sourceTypeModel)
            assertEquals("1.0", source?.sourceTypeCatalogVersion)
            assertEquals("Test Source", source?.expectedSourceName)
            assertEquals("src-123", source?.sourceId)
            assertEquals("Test Source Name", source?.sourceName)
            assertEquals(mapOf("key1" to "value1"), source?.attributes)
            assertTrue(source?.assigned ?: false)

            // Test attributes
            assertEquals(mapOf("key1" to "value1", "key2" to "value2"), user?.attributes)
        }

    @Test
    fun `test getUser returns null for non-existent user`() =
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

            val client = RadarClient(config, testTokenProvider, ktorClientFactory)
            val user = client.getUser("project-1", "non-existent")

            assertNull(user)
        }

    @Test
    fun `test getUsers handles empty response`() =
        runBlocking {
            mockEngine =
                MockEngine { _: HttpRequestData ->
                    respond(
                        content = ByteReadChannel("[]"),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            mockClient = HttpClient(mockEngine)
            every { ktorClientFactory.createClient(any(), any(), any()) } returns mockClient

            val testTokenProvider =
                mockk<ServiceTokenProvider>(relaxed = true).apply {
                    coEvery { getToken() } returns "test-token"
                }

            val client = RadarClient(config, testTokenProvider, ktorClientFactory)
            val users = client.getUsers("project-1")

            assertNotNull(users)
            assertTrue(users.isEmpty())
        }
}
