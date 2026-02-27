package org.radarbase.project.service

import org.radarbase.core.model.project.Group
import org.radarbase.core.model.project.Project
import org.radarbase.project.model.CreateGroupRequest
import org.radarbase.project.model.ProjectParticipant
import org.radarbase.project.model.RadarProject

interface ProjectService {
    suspend fun getProjects(authToken: String?): List<Project>

    suspend fun getProject(
        projectId: Long,
        authToken: String?,
    ): Project?

    suspend fun getProjectParticipants(
        projectId: Long,
        authToken: String?,
    ): List<ProjectParticipant>

    suspend fun getProjectParticipant(
        projectId: Long,
        participantId: String,
        authToken: String?,
    ): ProjectParticipant?

    suspend fun listGroups(
        projectName: String,
        authToken: String?,
    ): List<Group>

    suspend fun createGroup(
        projectName: String,
        request: CreateGroupRequest,
        authToken: String?,
    ): Group?

    suspend fun deleteGroup(
        projectName: String,
        groupName: String,
        unlinkSubjects: Boolean = false,
        authToken: String?,
    ): Boolean
}

data class CombinedProject(
    val id: String,
    val radarInfo: RadarProject,
    val project: Project,
)
