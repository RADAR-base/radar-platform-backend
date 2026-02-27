package org.radarbase.delegate.service

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.radarbase.core.model.project.Group
import org.radarbase.core.model.project.Project
import org.radarbase.delegate.config.DelegateConfig
import org.radarbase.delegate.model.ProjectParticipant
import org.slf4j.LoggerFactory

@Singleton
class ProjectServiceImpl
    @Inject
    constructor(
        private val httpClient: HttpClientService,
        config: DelegateConfig,
    ) : ProjectService {
        private val logger = LoggerFactory.getLogger(ProjectServiceImpl::class.java)
        private val baseUrl = config.projectService.baseUrl

        override suspend fun getProjects(authToken: String?): List<Project> {
            logger.debug("Fetching all projects")
            return httpClient.getJson("$baseUrl/projects", authToken)
        }

        override suspend fun getProject(
            projectId: String,
            authToken: String?,
        ): Project? {
            logger.debug("Fetching project $projectId")
            return httpClient.getJson("$baseUrl/projects/$projectId", authToken)
        }

        override suspend fun getProjectParticipants(
            projectId: String,
            authToken: String?,
        ): List<ProjectParticipant> {
            logger.debug("Fetching participants for project $projectId")
            return httpClient.getJson("$baseUrl/projects/$projectId/participants", authToken)
        }

        override suspend fun getProjectParticipant(
            projectId: String,
            participantId: String,
            authToken: String?,
        ): ProjectParticipant? {
            logger.debug("Fetching participant $participantId for project $projectId")
            return httpClient.getJson("$baseUrl/projects/$projectId/participants/$participantId", authToken)
        }

        override suspend fun listGroups(
            projectName: String,
            authToken: String?,
        ): List<Group> {
            logger.debug("Listing groups for project $projectName")
            return httpClient.getJson("$baseUrl/projects/$projectName/groups", authToken)
        }

        override suspend fun createGroup(
            projectName: String,
            body: String,
            authToken: String?,
        ): Pair<Int, String> {
            logger.debug("Creating group for project $projectName")
            return httpClient.postWithBody("$baseUrl/projects/$projectName/groups", body, authToken)
        }

        override suspend fun deleteGroup(
            projectName: String,
            groupName: String,
            unlinkSubjects: Boolean,
            authToken: String?,
        ): Pair<Int, String> {
            logger.debug("Deleting group $groupName for project $projectName")
            val url =
                if (unlinkSubjects) {
                    "$baseUrl/projects/$projectName/groups/$groupName?unlinkSubjects=true"
                } else {
                    "$baseUrl/projects/$projectName/groups/$groupName"
                }
            return httpClient.delete(url, authToken)
        }
    }
