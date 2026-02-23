package org.radarbase.delegate.service

import org.radarbase.delegate.model.Group
import org.radarbase.delegate.model.Project
import org.radarbase.delegate.model.ProjectParticipant

interface ProjectService {
    suspend fun getProjects(authToken: String?): List<Project>

    suspend fun getProject(
        projectId: String,
        authToken: String?,
    ): Project?

    suspend fun getProjectParticipants(
        projectId: String,
        authToken: String?,
    ): List<ProjectParticipant>

    suspend fun getProjectParticipant(
        projectId: String,
        participantId: String,
        authToken: String?,
    ): ProjectParticipant?

    suspend fun listGroups(
        projectName: String,
        authToken: String?,
    ): List<Group>

    suspend fun createGroup(
        projectName: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String>

    suspend fun deleteGroup(
        projectName: String,
        groupName: String,
        unlinkSubjects: Boolean,
        authToken: String?,
    ): Pair<Int, String>
}
