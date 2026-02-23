package org.radarbase.config.provider

import org.radarbase.config.model.StudyConfig
import org.radarbase.config.model.StudyDefinition

/**
 * Abstraction for retrieving and optionally persisting study configuration by project name.
 *
 * Implementations can pull from GitHub, local files, databases, etc.
 * [ProviderConfig.order] is used for both read and write (first enabled writable provider is used for writes).
 * Providers that support writes override [supportsWrites] and the write methods.
 */
interface StudyConfigProvider {
    val id: String

    /** True if this provider can persist study config, protocol body, and questionnaire bodies. */
    fun supportsWrites(): Boolean = false

    suspend fun getStudyConfig(projectName: String): StudyConfig?

    suspend fun saveStudyDefinition(
        projectName: String,
        definition: StudyDefinition,
    ): Unit = throw UnsupportedOperationException("Provider $id does not support writes")

    fun getProtocolBody(projectName: String): String? = null

    fun putProtocolBody(
        projectName: String,
        body: String,
    ): Unit = throw UnsupportedOperationException("Provider $id does not support writes")

    fun getEnrolmentLandingBody(projectName: String): String? = null

    fun putEnrolmentLandingBody(
        projectName: String,
        body: String,
    ): Unit = throw UnsupportedOperationException("Provider $id does not support writes")

    fun getEnrolmentProtocolBody(projectName: String): String? = null

    fun putEnrolmentProtocolBody(
        projectName: String,
        body: String,
    ): Unit = throw UnsupportedOperationException("Provider $id does not support writes")

    fun getQuestionnaireBody(
        projectName: String,
        questionnaireId: String,
    ): String? = null

    fun putQuestionnaireBody(
        projectName: String,
        questionnaireId: String,
        body: String,
    ): Unit = throw UnsupportedOperationException("Provider $id does not support writes")

    /** URL where questionnaire body is or will be stored (e.g. raw GitHub URL). Used when creating a questionnaire so study definition can be saved with correct href after body is written. */
    fun getQuestionnaireBodyHref(
        projectName: String,
        questionnaireId: String,
    ): String? = null

    /** Writes questionnaire body to storage only, without updating study definition. Used by createQuestionnaire so body is written first, then study definition is saved. */
    fun putQuestionnaireBodyOnly(
        projectName: String,
        questionnaireId: String,
        body: String,
    ) {
        putQuestionnaireBody(projectName, questionnaireId, body)
    }
}
