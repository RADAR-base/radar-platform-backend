package org.radarbase.config.service

import org.radarbase.config.model.CreateQuestionnaireRequest
import org.radarbase.config.model.Questionnaire
import org.radarbase.config.model.SourceRef
import org.radarbase.config.model.StudyConfig
import org.radarbase.config.model.StudyDefinition

interface StudyConfigService {
    suspend fun getStudyConfig(projectName: String): StudyConfig?

    suspend fun getStudyDefinition(projectName: String): StudyDefinition?

    fun getProtocolBodyOverride(projectName: String): String?

    fun getEnrolmentLandingBodyOverride(projectName: String): String?

    fun getEnrolmentProtocolBodyOverride(projectName: String): String?

    fun getQuestionnaireBodyOverride(
        projectName: String,
        questionnaireId: String,
    ): String?

    suspend fun updateStudyConfig(
        projectName: String,
        definition: StudyDefinition,
    )

    suspend fun updateQuestionnaires(
        projectName: String,
        questionnaires: List<Questionnaire>,
    )

    suspend fun createQuestionnaire(
        projectName: String,
        request: CreateQuestionnaireRequest,
    )

    suspend fun updateQuestionnaire(
        projectName: String,
        questionnaireId: String,
        questionnaire: Questionnaire,
        body: String? = null,
    )

    suspend fun deleteQuestionnaire(
        projectName: String,
        questionnaireId: String,
    )

    suspend fun updateProtocolSource(
        projectName: String,
        protocol: SourceRef,
    )

    suspend fun updateProtocolBody(
        projectName: String,
        body: String,
    )

    suspend fun updateEnrolmentSource(
        projectName: String,
        enrolment: SourceRef,
    )

    suspend fun updateEnrolmentLandingBody(
        projectName: String,
        body: String,
    )

    suspend fun updateEnrolmentProtocolBody(
        projectName: String,
        body: String,
    )
}
