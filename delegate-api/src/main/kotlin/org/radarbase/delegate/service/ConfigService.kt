package org.radarbase.delegate.service

interface ConfigService {
    suspend fun getStudyConfig(
        projectId: String,
        authToken: String?,
    ): String

    suspend fun getProtocol(
        projectId: String,
        authToken: String?,
    ): String

    suspend fun getEnrolmentSource(
        projectId: String,
        authToken: String?,
    ): String

    suspend fun getEnrolmentLanding(
        projectId: String,
        authToken: String?,
    ): String

    suspend fun getEnrolmentProtocol(
        projectId: String,
        authToken: String?,
    ): String

    suspend fun getQuestionnaires(
        projectId: String,
        authToken: String?,
    ): String

    suspend fun getQuestionnaire(
        projectId: String,
        questionnaireId: String,
        authToken: String?,
    ): String

    suspend fun updateStudyConfig(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String>

    suspend fun updateQuestionnaires(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String>

    suspend fun createQuestionnaire(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String>

    suspend fun updateQuestionnaire(
        projectId: String,
        questionnaireId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String>

    suspend fun deleteQuestionnaire(
        projectId: String,
        questionnaireId: String,
        authToken: String?,
    ): Pair<Int, String>

    suspend fun updateProtocolSource(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String>

    suspend fun updateProtocolBody(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String>

    suspend fun updateEnrolmentSource(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String>

    suspend fun updateEnrolmentLandingBody(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String>

    suspend fun updateEnrolmentProtocolBody(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String>
}
