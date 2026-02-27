package org.radarbase.delegate.service

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.radarbase.delegate.config.DelegateConfig
import org.radarbase.delegate.model.User
import org.slf4j.LoggerFactory

@Singleton
class UserServiceImpl
    @Inject
    constructor(
        private val httpClient: HttpClientService,
        config: DelegateConfig,
    ) : UserService {
        private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
        private val baseUrl = config.userService.baseUrl

        override suspend fun getUsers(
            projectId: String,
            authToken: String?,
        ): List<User> {
            logger.debug("Fetching users for project $projectId")
            return httpClient.getJson("$baseUrl/project/$projectId/users", authToken)
        }

        override suspend fun getUser(
            projectId: String,
            userId: String,
            authToken: String?,
        ): User? {
            logger.debug("Fetching user $userId for project $projectId")
            return httpClient.getJson("$baseUrl/project/$projectId/user/$userId", authToken)
        }

        override suspend fun createUser(
            projectId: String,
            body: String,
            authToken: String?,
        ): Pair<Int, String> {
            logger.debug("Creating user for project $projectId")
            return httpClient.postWithBody("$baseUrl/project/$projectId/users", body, authToken)
        }

        override suspend fun updateUser(
            projectId: String,
            userId: String,
            body: String,
            authToken: String?,
        ): Pair<Int, String> {
            logger.debug("Updating user $userId for project $projectId")
            return httpClient.putWithBody("$baseUrl/project/$projectId/user/$userId", body, authToken)
        }
    }
