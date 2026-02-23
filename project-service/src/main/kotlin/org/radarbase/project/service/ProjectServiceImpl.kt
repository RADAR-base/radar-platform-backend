package org.radarbase.project.service

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.radarbase.project.client.RadarProjectClient
import org.radarbase.project.model.CreateGroupRequest
import org.radarbase.project.model.Group
import org.radarbase.project.model.Project
import org.radarbase.project.model.ProjectParticipant
import org.radarbase.project.model.RadarProject

@Singleton
class ProjectServiceImpl
    @Inject
    constructor(
        private val radarProjectClient: RadarProjectClient,
    ) : ProjectService {
        // TODO: Combine with project data from SEP protocol json on github

        private fun RadarProject.toProject(): Project =
            Project(
                projectId = attributes["External-project-id"] ?: id.toString(),
                name = humanReadableProjectName ?: projectName,
                description = description ?: "",
                status = projectStatus ?: "UNKNOWN",
                startDate = startDate ?: "",
                endDate = endDate ?: "",
                location = location ?: "",
                population =
                    org.radarbase.project.model.Population(
                        ageRange =
                            org.radarbase.project.model.AgeRange(
                                min = 0,
                                max = 0,
                            ),
                        maxParticipants = 0,
                    ),
                domain =
                    org.radarbase.project.model.Domain(
                        area = "",
                        keywords = emptyList(),
                    ),
                eligibility =
                    org.radarbase.project.model.Eligibility(
                        inclusion = emptyList(),
                        exclusion = emptyList(),
                    ),
                design =
                    org.radarbase.project.model.Design(
                        phases = emptyList(),
                        measurements = emptyList(),
                        intervention =
                            org.radarbase.project.model.Intervention(
                                description = "",
                                deliveryModes = emptyList(),
                            ),
                        outcomes =
                            org.radarbase.project.model.Outcomes(
                                primary = emptyList(),
                                secondary = emptyList(),
                            ),
                    ),
                technology =
                    org.radarbase.project.model.Technology(
                        devices = emptyList(),
                        dataTypes = emptyList(),
                        frequency = "",
                        notes = null,
                    ),
                analysis =
                    org.radarbase.project.model.Analysis(
                        features = emptyList(),
                        visualizations = emptyList(),
                    ),
                contact =
                    org.radarbase.project.model.Contact(
                        email = "",
                        resources = emptyList(),
                    ),
            )

        override suspend fun getProjects(authToken: String?): List<Project> =
            radarProjectClient.getProjects(authToken).map { it.toProject() }

        override suspend fun getProject(
            projectId: Long,
            authToken: String?,
        ): Project? = radarProjectClient.getProject(projectId, authToken)?.toProject()

        override suspend fun getProjectParticipants(
            projectId: Long,
            authToken: String?,
        ): List<ProjectParticipant> = radarProjectClient.getProjectParticipants(projectId, authToken)

        override suspend fun getProjectParticipant(
            projectId: Long,
            participantId: String,
            authToken: String?,
        ): ProjectParticipant? = radarProjectClient.getProjectParticipant(projectId, participantId, authToken)

        override suspend fun listGroups(
            projectName: String,
            authToken: String?,
        ): List<Group> = radarProjectClient.listGroups(projectName, authToken)

        override suspend fun createGroup(
            projectName: String,
            request: CreateGroupRequest,
            authToken: String?,
        ): Group? = radarProjectClient.createGroup(projectName, request, authToken)

        override suspend fun deleteGroup(
            projectName: String,
            groupName: String,
            unlinkSubjects: Boolean,
            authToken: String?,
        ): Boolean = radarProjectClient.deleteGroup(projectName, groupName, unlinkSubjects, authToken)
    }
