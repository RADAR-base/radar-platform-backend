package org.radarbase.delegate.service

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.radarbase.contract.response.ProxyResponse
import org.radarbase.contract.service.ConfigServiceContract
import org.radarbase.contract.service.DataSourcesServiceContract
import org.radarbase.contract.service.ParticipantServiceContract
import org.radarbase.contract.service.ProjectServiceContract
import org.radarbase.contract.service.UserServiceContract
import org.radarbase.contract.utils.ContractUtils.deserializeDtoFromContract
import org.radarbase.core.model.user.User
import org.radarbase.delegate.config.DelegateConfig
import org.radarbase.delegate.model.Participant
import org.radarbase.jersey.service.ProjectService

@Singleton
class DelegateApiService
@Inject
constructor(
    private val projectService: ProjectService,
    private val config: DelegateConfig,
) {
    private val userCache = Cache<List<User>>(10L)
    private val participantCache = Cache<List<Participant>>(10L)

    // ------------------------------------------------------------------ //
    //  User Service
    // ------------------------------------------------------------------ //

    suspend fun getUsers(projectId: String, authToken: String?): List<User> {
        projectService.ensureProject(projectId)
        return userCache.withCache(
            cacheKey = "users:$projectId",
            fetchData = {
                UserServiceContract.getUsers(config.contract.user, projectId, authToken).let {
                    deserializeDtoFromContract<List<User>>(it) {
                        "users_not_found ; Users not found for project $projectId"
                    }
                }
            },
            logMessage = "Returning cached users for project $projectId",
        )
    }

    suspend fun getUser(projectId: String, userId: String, authToken: String?): User? {
        projectService.ensureProject(projectId)
        return getUsers(projectId, authToken).find { it.id == userId }
    }

    suspend fun createUser(projectId: String, body: String, authToken: String?): ProxyResponse {
        projectService.ensureProject(projectId)
        return UserServiceContract.createUser(config.contract.user, projectId, body, authToken)
            .also { userCache.clearCache("users:$projectId") }
    }

    suspend fun updateUser(projectId: String, userId: String, body: String, authToken: String?): ProxyResponse {
        projectService.ensureProject(projectId)
        return UserServiceContract.updateUser(config.contract.user, projectId, userId, body, authToken)
            .also { userCache.clearCache("users:$projectId") }
    }

    suspend fun getParticipants(projectId: String, authToken: String?): List<Participant> {
        projectService.ensureProject(projectId)
        return participantCache.withCache(
            cacheKey = "participants:$projectId",
            fetchData = {
                ParticipantServiceContract.getParticipants(config.contract.participant, projectId, authToken).let {
                    deserializeDtoFromContract<List<Participant>>(it) {
                        "participants_not_found ; Participants not found for project $projectId"
                    }
                }
            },
            logMessage = "Returning cached participants for project $projectId",
        )
    }

    suspend fun getParticipant(projectId: String, participantId: String, authToken: String?): Participant? {
        projectService.ensureProject(projectId)
        return getParticipants(projectId, authToken).find { it.id == participantId }
    }

    suspend fun createParticipant(projectId: String, body: String, authToken: String?): ProxyResponse {
        projectService.ensureProject(projectId)
        return ParticipantServiceContract.createParticipant(config.contract.participant, projectId, body, authToken)
            .also { participantCache.clearCache("participants:$projectId") }
    }

    suspend fun updateParticipant(projectId: String, participantId: String, body: String, authToken: String?): ProxyResponse {
        projectService.ensureProject(projectId)
        return ParticipantServiceContract.updateParticipant(config.contract.participant, projectId, participantId, body, authToken)
            .also { participantCache.clearCache("participants:$projectId") }
    }

    // ------------------------------------------------------------------ //
    //  Project Service
    // ------------------------------------------------------------------ //

    suspend fun getProjects(authToken: String?): ProxyResponse =
        ProjectServiceContract.getProjects(config.contract.project, authToken)

    suspend fun getProject(projectId: String, authToken: String?): ProxyResponse =
        ProjectServiceContract.getProject(config.contract.project, projectId, authToken)

    suspend fun getProjectParticipants(projectId: String, authToken: String?): ProxyResponse =
        ProjectServiceContract.getProjectParticipants(config.contract.project, projectId, authToken)

    suspend fun getProjectParticipant(projectId: String, participantId: String, authToken: String?): ProxyResponse =
        ProjectServiceContract.getProjectParticipant(config.contract.project, projectId, participantId, authToken)

    suspend fun listGroups(projectName: String, authToken: String?): ProxyResponse {
        projectService.ensureProject(projectName)
        return ProjectServiceContract.listGroups(config.contract.project, projectName, authToken)
    }

    suspend fun createGroup(projectName: String, body: String, authToken: String?): ProxyResponse {
        projectService.ensureProject(projectName)
        return ProjectServiceContract.createGroup(config.contract.project, projectName, body, authToken)
    }

    suspend fun deleteGroup(projectName: String, groupName: String, unlinkSubjects: Boolean, authToken: String?): ProxyResponse {
        projectService.ensureProject(projectName)
        return ProjectServiceContract.deleteGroup(config.contract.project, projectName, groupName, unlinkSubjects, authToken)
    }

    // ------------------------------------------------------------------ //
    //  Config Service
    // ------------------------------------------------------------------ //

    suspend fun getStudyConfig(projectId: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.getStudyConfig(config.contract.config, projectId, authToken)

    suspend fun getProtocol(projectId: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.getProtocol(config.contract.config, projectId, authToken)

    suspend fun getEnrolmentSource(projectId: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.getEnrolmentSource(config.contract.config, projectId, authToken)

    suspend fun getEnrolmentLanding(projectId: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.getEnrolmentLanding(config.contract.config, projectId, authToken)

    suspend fun getEnrolmentProtocol(projectId: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.getEnrolmentProtocol(config.contract.config, projectId, authToken)

    suspend fun getQuestionnaires(projectId: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.getQuestionnaires(config.contract.config, projectId, authToken)

    suspend fun getQuestionnaire(projectId: String, questionnaireId: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.getQuestionnaire(config.contract.config, projectId, questionnaireId, authToken)

    suspend fun updateStudyConfig(projectId: String, body: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.updateStudyConfig(config.contract.config, projectId, body, authToken)

    suspend fun updateQuestionnaires(projectId: String, body: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.updateQuestionnaires(config.contract.config, projectId, body, authToken)

    suspend fun createQuestionnaire(projectId: String, body: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.createQuestionnaire(config.contract.config, projectId, body, authToken)

    suspend fun updateQuestionnaire(projectId: String, questionnaireId: String, body: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.updateQuestionnaire(config.contract.config, projectId, questionnaireId, body, authToken)

    suspend fun deleteQuestionnaire(projectId: String, questionnaireId: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.deleteQuestionnaire(config.contract.config, projectId, questionnaireId, authToken)

    suspend fun updateProtocolSource(projectId: String, body: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.updateProtocolSource(config.contract.config, projectId, body, authToken)

    suspend fun updateProtocolBody(projectId: String, body: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.updateProtocolBody(config.contract.config, projectId, body, authToken)

    suspend fun updateEnrolmentSource(projectId: String, body: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.updateEnrolmentSource(config.contract.config, projectId, body, authToken)

    suspend fun updateEnrolmentLandingBody(projectId: String, body: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.updateEnrolmentLandingBody(config.contract.config, projectId, body, authToken)

    suspend fun updateEnrolmentProtocolBody(projectId: String, body: String, authToken: String?): ProxyResponse =
        ConfigServiceContract.updateEnrolmentProtocolBody(config.contract.config, projectId, body, authToken)

    // ------------------------------------------------------------------ //
    //  Data Sources Service
    // ------------------------------------------------------------------ //

    suspend fun getSourceCatalog(): ProxyResponse =
        DataSourcesServiceContract.getSourceCatalog(config.contract.dataSources)

    suspend fun getParticipantSources(projectId: String, participantId: String): ProxyResponse =
        DataSourcesServiceContract.getParticipantSources(config.contract.dataSources, projectId, participantId)
}
