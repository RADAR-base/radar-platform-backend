package org.radarbase.delegate.service

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.radarbase.core.model.project.Group
import org.radarbase.core.model.project.Project
import org.radarbase.delegate.model.Participant
import org.radarbase.delegate.model.ProjectParticipant
import org.radarbase.delegate.model.User
import org.radarbase.jersey.service.ProjectService

@Singleton
class DelegateApiService
@Inject
constructor(
    private val projectService: ProjectService,
    private val delegateProjectService: org.radarbase.delegate.service.ProjectService,
    private val userService: UserService,
    private val participantService: ParticipantService,
    private val configService: ConfigService,
    private val dataSourcesService: DataSourcesService,
) {
    private val userCache = Cache<List<User>>(10L)
    private val participantCache = Cache<List<Participant>>(10L)

    suspend fun getUsers(
        projectId: String,
        authToken: String?,
    ): List<User> {
        projectService.ensureProject(projectId)
        return userCache.withCache(
            cacheKey = "users:$projectId",
            fetchData = { userService.getUsers(projectId, authToken) },
            logMessage = "Returning cached users for project $projectId",
        )
    }

    suspend fun getUser(
        projectId: String,
        userId: String,
        authToken: String?,
    ): User? {
        projectService.ensureProject(projectId)
        return getUsers(projectId, authToken).find { it.id == userId }
    }

    suspend fun createUser(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String> {
        projectService.ensureProject(projectId)
        val result = userService.createUser(projectId, body, authToken)
        userCache.clearCache("users:$projectId")
        return result
    }

    suspend fun updateUser(
        projectId: String,
        userId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String> {
        projectService.ensureProject(projectId)
        val result = userService.updateUser(projectId, userId, body, authToken)
        userCache.clearCache("users:$projectId")
        return result
    }

    suspend fun getParticipants(
        projectId: String,
        authToken: String?,
    ): List<Participant> {
        projectId.let { projectService.ensureProject(it) }
        return participantCache.withCache(
            cacheKey = "projectId",
            fetchData = { participantService.getParticipants(projectId, authToken) },
            logMessage = "Returning cached participants for project $projectId",
        )
    }

    suspend fun getParticipant(
        projectId: String,
        participantId: String,
        authToken: String?,
    ): Participant? {
        projectService.ensureProject(projectId)
        return getParticipants(projectId, authToken).find { it.id == participantId }
    }

    suspend fun createParticipant(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String> {
        projectService.ensureProject(projectId)
        val result = participantService.createParticipant(projectId, body, authToken)
        participantCache.clearCache("projectId")
        return result
    }

    suspend fun updateParticipant(
        projectId: String,
        participantId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String> {
        projectService.ensureProject(projectId)
        val result = participantService.updateParticipant(projectId, participantId, body, authToken)
        participantCache.clearCache("projectId")
        return result
    }

    suspend fun getProjects(authToken: String?): List<Project> = delegateProjectService.getProjects(authToken)

    suspend fun getProject(
        projectId: String,
        authToken: String?,
    ): Project? = delegateProjectService.getProject(projectId, authToken)

    suspend fun getProjectParticipants(
        projectId: String,
        authToken: String?,
    ): List<ProjectParticipant> = delegateProjectService.getProjectParticipants(projectId, authToken)

    suspend fun getProjectParticipant(
        projectId: String,
        participantId: String,
        authToken: String?,
    ): ProjectParticipant? = delegateProjectService.getProjectParticipant(projectId, participantId, authToken)

    suspend fun listGroups(
        projectName: String,
        authToken: String?,
    ): List<Group> {
        projectService.ensureProject(projectName)
        return delegateProjectService.listGroups(projectName, authToken)
    }

    suspend fun createGroup(
        projectName: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String> {
        projectService.ensureProject(projectName)
        return delegateProjectService.createGroup(projectName, body, authToken)
    }

    suspend fun deleteGroup(
        projectName: String,
        groupName: String,
        unlinkSubjects: Boolean,
        authToken: String?,
    ): Pair<Int, String> {
        projectService.ensureProject(projectName)
        return delegateProjectService.deleteGroup(projectName, groupName, unlinkSubjects, authToken)
    }

    suspend fun getStudyConfig(
        projectId: String,
        authToken: String?,
    ): String = configService.getStudyConfig(projectId, authToken)

    suspend fun getProtocol(
        projectId: String,
        authToken: String?,
    ): String = configService.getProtocol(projectId, authToken)

    suspend fun getEnrolmentSource(
        projectId: String,
        authToken: String?,
    ): String = configService.getEnrolmentSource(projectId, authToken)

    suspend fun getEnrolmentLanding(
        projectId: String,
        authToken: String?,
    ): String = configService.getEnrolmentLanding(projectId, authToken)

    suspend fun getEnrolmentProtocol(
        projectId: String,
        authToken: String?,
    ): String = configService.getEnrolmentProtocol(projectId, authToken)

    suspend fun getQuestionnaires(
        projectId: String,
        authToken: String?,
    ): String = configService.getQuestionnaires(projectId, authToken)

    suspend fun getQuestionnaire(
        projectId: String,
        questionnaireId: String,
        authToken: String?,
    ): String = configService.getQuestionnaire(projectId, questionnaireId, authToken)

    suspend fun updateStudyConfig(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String> = configService.updateStudyConfig(projectId, body, authToken)

    suspend fun updateQuestionnaires(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String> = configService.updateQuestionnaires(projectId, body, authToken)

    suspend fun createQuestionnaire(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String> = configService.createQuestionnaire(projectId, body, authToken)

    suspend fun updateQuestionnaire(
        projectId: String,
        questionnaireId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String> = configService.updateQuestionnaire(projectId, questionnaireId, body, authToken)

    suspend fun deleteQuestionnaire(
        projectId: String,
        questionnaireId: String,
        authToken: String?,
    ): Pair<Int, String> = configService.deleteQuestionnaire(projectId, questionnaireId, authToken)

    suspend fun updateProtocolSource(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String> = configService.updateProtocolSource(projectId, body, authToken)

    suspend fun updateProtocolBody(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String> = configService.updateProtocolBody(projectId, body, authToken)

    suspend fun updateEnrolmentSource(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String> = configService.updateEnrolmentSource(projectId, body, authToken)

    suspend fun updateEnrolmentLandingBody(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String> = configService.updateEnrolmentLandingBody(projectId, body, authToken)

    suspend fun updateEnrolmentProtocolBody(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String> = configService.updateEnrolmentProtocolBody(projectId, body, authToken)

    suspend fun getSourceCatalog(): String = dataSourcesService.getSourceCatalog()

    suspend fun getParticipantSources(
        projectId: String,
        participantId: String,
    ): String = dataSourcesService.getParticipantSources(projectId, participantId)
}
