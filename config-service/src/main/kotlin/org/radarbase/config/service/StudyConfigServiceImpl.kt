package org.radarbase.config.service

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import org.radarbase.config.config.ConfigServiceConfig
import org.radarbase.config.model.CreateQuestionnaireRequest
import org.radarbase.config.model.Questionnaire
import org.radarbase.config.model.SourceRef
import org.radarbase.config.model.StudyConfig
import org.radarbase.config.model.StudyDefinition
import org.radarbase.config.provider.AppConfigStudyConfigProvider
import org.radarbase.config.provider.DbStudyConfigProvider
import org.radarbase.config.provider.GithubStudyConfigProvider
import org.radarbase.config.provider.StudyConfigProvider
import org.slf4j.LoggerFactory

@Singleton
class StudyConfigServiceImpl
    @Inject
    constructor(
        private val config: ConfigServiceConfig,
        private val githubProvider: GithubStudyConfigProvider,
        private val appConfigProvider: AppConfigStudyConfigProvider,
        private val dbProvider: DbStudyConfigProvider,
    ) : StudyConfigService {
        private val logger = LoggerFactory.getLogger(StudyConfigServiceImpl::class.java)

        private val providersById: Map<String, StudyConfigProvider> =
            mapOf(
                githubProvider.id to githubProvider,
                appConfigProvider.id to appConfigProvider,
                dbProvider.id to dbProvider,
            )

        private val json =
            Json {
                ignoreUnknownKeys = true
            }

        private fun firstWritableProvider(): StudyConfigProvider? =
            config.providers.order.firstNotNullOfOrNull { providerId ->
                val provider = providersById[providerId] ?: return@firstNotNullOfOrNull null
                val enabled =
                    when (providerId) {
                        "db" -> config.providers.db.enabled
                        "appConfig" -> config.providers.appConfig.enabled
                        "github" -> config.providers.github.enabled
                        else -> false
                    }
                if (enabled && provider.supportsWrites()) provider else null
            }

        override suspend fun getStudyConfig(projectName: String): StudyConfig? {
            val order = config.providers.order

            for (providerId in order) {
                val provider = providersById[providerId]
                if (provider == null) {
                    logger.warn("StudyConfigProvider with id '{}' not found, skipping", providerId)
                    continue
                }

                val result = provider.getStudyConfig(projectName)
                if (result != null) {
                    logger.info("Study config for project {} resolved by provider {}", projectName, provider.id)
                    return result
                }
            }

            logger.warn("No study config found for project {} using providers {}", projectName, order)
            return null
        }

        override suspend fun getStudyDefinition(projectName: String): StudyDefinition? {
            val result = getStudyConfig(projectName) ?: return null
            return result.config
        }

        override fun getProtocolBodyOverride(projectName: String): String? =
            config.providers.order.firstNotNullOfOrNull { providerId ->
                val provider = providersById[providerId] ?: return@firstNotNullOfOrNull null
                val enabled =
                    when (providerId) {
                        "db" -> config.providers.db.enabled
                        "appConfig" -> config.providers.appConfig.enabled
                        "github" -> config.providers.github.enabled
                        else -> false
                    }
                if (enabled && provider.supportsWrites()) provider.getProtocolBody(projectName) else null
            }

        override fun getEnrolmentLandingBodyOverride(projectName: String): String? =
            config.providers.order.firstNotNullOfOrNull { providerId ->
                val provider = providersById[providerId] ?: return@firstNotNullOfOrNull null
                val enabled =
                    when (providerId) {
                        "db" -> config.providers.db.enabled
                        "appConfig" -> config.providers.appConfig.enabled
                        "github" -> config.providers.github.enabled
                        else -> false
                    }
                if (enabled && provider.supportsWrites()) provider.getEnrolmentLandingBody(projectName) else null
            }

        override fun getEnrolmentProtocolBodyOverride(projectName: String): String? =
            config.providers.order.firstNotNullOfOrNull { providerId ->
                val provider = providersById[providerId] ?: return@firstNotNullOfOrNull null
                val enabled =
                    when (providerId) {
                        "db" -> config.providers.db.enabled
                        "appConfig" -> config.providers.appConfig.enabled
                        "github" -> config.providers.github.enabled
                        else -> false
                    }
                if (enabled && provider.supportsWrites()) provider.getEnrolmentProtocolBody(projectName) else null
            }

        override fun getQuestionnaireBodyOverride(
            projectName: String,
            questionnaireId: String,
        ): String? =
            config.providers.order.firstNotNullOfOrNull { providerId ->
                val provider = providersById[providerId] ?: return@firstNotNullOfOrNull null
                val enabled =
                    when (providerId) {
                        "db" -> config.providers.db.enabled
                        "appConfig" -> config.providers.appConfig.enabled
                        "github" -> config.providers.github.enabled
                        else -> false
                    }
                if (enabled && provider.supportsWrites()) provider.getQuestionnaireBody(projectName, questionnaireId) else null
            }

        override suspend fun updateStudyConfig(
            projectName: String,
            definition: StudyDefinition,
        ) {
            val writable =
                firstWritableProvider()
                    ?: throw IllegalStateException("No writable provider enabled (order: ${config.providers.order})")
            logger.info(
                "[CONFIG WRITE] Saving study config for project {} -> provider '{}' (order: {})",
                projectName,
                writable.id,
                config.providers.order,
            )
            writable.saveStudyDefinition(projectName, definition)
            logger.info("[CONFIG WRITE] Study config saved for project {} via provider '{}'", projectName, writable.id)
        }

        override suspend fun updateQuestionnaires(
            projectName: String,
            questionnaires: List<Questionnaire>,
        ) {
            val current =
                getStudyDefinition(projectName)
                    ?: throw NoSuchElementException("Study config not found for project $projectName")
            val updated = current.copy(questionnaires = questionnaires)
            logger.info(
                "[CONFIG WRITE] Saving questionnaires for project {} ({} items) -> provider from updateStudyConfig",
                projectName,
                questionnaires.size,
            )
            updateStudyConfig(projectName, updated)
        }

        override suspend fun createQuestionnaire(
            projectName: String,
            request: CreateQuestionnaireRequest,
        ) {
            val current =
                getStudyDefinition(projectName)
                    ?: throw NoSuchElementException("Study config not found for project $projectName")
            val existing = current.questionnaires.any { it.id == request.questionnaire.id }
            if (existing) {
                throw IllegalArgumentException("Questionnaire ${request.questionnaire.id} already exists")
            }
            val writable =
                firstWritableProvider()
                    ?: throw IllegalStateException("No writable provider enabled (order: ${config.providers.order})")
            val href =
                writable.getQuestionnaireBodyHref(projectName, request.questionnaire.id)
                    ?: request.questionnaire.href
            logger.info(
                "[CONFIG WRITE] Creating questionnaire {} for project {} -> provider '{}' (order: {})",
                request.questionnaire.id,
                projectName,
                writable.id,
                config.providers.order,
            )
            request.body?.let { writable.putQuestionnaireBodyOnly(projectName, request.questionnaire.id, it) }
            val questionnaireWithHref = request.questionnaire.copy(href = href)
            val updated = current.copy(questionnaires = current.questionnaires + questionnaireWithHref)
            writable.saveStudyDefinition(projectName, updated)
            logger.info(
                "[CONFIG WRITE] Questionnaire {} created for project {} via provider '{}'",
                request.questionnaire.id,
                projectName,
                writable.id,
            )
        }

        override suspend fun updateQuestionnaire(
            projectName: String,
            questionnaireId: String,
            questionnaire: Questionnaire,
            body: String?,
        ) {
            val current =
                getStudyDefinition(projectName)
                    ?: throw NoSuchElementException("Study config not found for project $projectName")
            val index = current.questionnaires.indexOfFirst { it.id == questionnaireId }
            if (index < 0) {
                throw NoSuchElementException("Questionnaire $questionnaireId not found for project $projectName")
            }
            val writable =
                firstWritableProvider()
                    ?: throw IllegalStateException("No writable provider enabled (order: ${config.providers.order})")
            val questionnaireWithHref =
                if (body != null) {
                    writable.putQuestionnaireBodyOnly(projectName, questionnaireId, body)
                    val href = writable.getQuestionnaireBodyHref(projectName, questionnaireId) ?: questionnaire.href
                    questionnaire.copy(href = href)
                } else {
                    questionnaire
                }
            val updatedList = current.questionnaires.toMutableList().apply { set(index, questionnaireWithHref) }
            val updated = current.copy(questionnaires = updatedList)
            logger.info(
                "[CONFIG WRITE] Updating questionnaire {} for project {} -> provider '{}' (body={})",
                questionnaireId,
                projectName,
                writable.id,
                body != null,
            )
            writable.saveStudyDefinition(projectName, updated)
        }

        override suspend fun deleteQuestionnaire(
            projectName: String,
            questionnaireId: String,
        ) {
            val current =
                getStudyDefinition(projectName)
                    ?: throw NoSuchElementException("Study config not found for project $projectName")
            val updatedList = current.questionnaires.filter { it.id != questionnaireId }
            if (updatedList.size == current.questionnaires.size) {
                throw NoSuchElementException("Questionnaire $questionnaireId not found for project $projectName")
            }
            val writable =
                firstWritableProvider()
                    ?: throw IllegalStateException("No writable provider enabled (order: ${config.providers.order})")
            val updated = current.copy(questionnaires = updatedList)
            logger.info(
                "[CONFIG WRITE] Deleting questionnaire {} from project {} -> provider '{}'",
                questionnaireId,
                projectName,
                writable.id,
            )
            writable.saveStudyDefinition(projectName, updated)
        }

        override suspend fun updateProtocolSource(
            projectName: String,
            protocol: SourceRef,
        ) {
            val current =
                getStudyDefinition(projectName)
                    ?: throw NoSuchElementException("Study config not found for project $projectName")
            val updated =
                current.copy(
                    sources = current.sources.copy(protocol = protocol),
                )
            logger.info("[CONFIG WRITE] Updating protocol source for project {} -> provider from updateStudyConfig", projectName)
            updateStudyConfig(projectName, updated)
        }

        override suspend fun updateProtocolBody(
            projectName: String,
            body: String,
        ) {
            val writable =
                firstWritableProvider()
                    ?: throw IllegalStateException("No writable provider enabled (order: ${config.providers.order})")
            logger.info(
                "[CONFIG WRITE] Saving protocol body for project {} -> provider '{}' (order: {})",
                projectName,
                writable.id,
                config.providers.order,
            )
            writable.putProtocolBody(projectName, body)
            logger.info("[CONFIG WRITE] Protocol body saved for project {} via provider '{}'", projectName, writable.id)
        }

        override suspend fun updateEnrolmentSource(
            projectName: String,
            enrolment: SourceRef,
        ) {
            val current =
                getStudyDefinition(projectName)
                    ?: throw NoSuchElementException("Study config not found for project $projectName")
            val updated =
                current.copy(
                    sources = current.sources.copy(enrolment = enrolment),
                )
            logger.info("[CONFIG WRITE] Updating enrolment source for project {} -> provider from updateStudyConfig", projectName)
            updateStudyConfig(projectName, updated)
        }

        override suspend fun updateEnrolmentLandingBody(
            projectName: String,
            body: String,
        ) {
            val writable =
                firstWritableProvider()
                    ?: throw IllegalStateException("No writable provider enabled (order: ${config.providers.order})")
            logger.info(
                "[CONFIG WRITE] Saving enrolment landing body for project {} -> provider '{}' (order: {})",
                projectName,
                writable.id,
                config.providers.order,
            )
            writable.putEnrolmentLandingBody(projectName, body)
            logger.info("[CONFIG WRITE] Enrolment landing body saved for project {} via provider '{}'", projectName, writable.id)
        }

        override suspend fun updateEnrolmentProtocolBody(
            projectName: String,
            body: String,
        ) {
            val writable =
                firstWritableProvider()
                    ?: throw IllegalStateException("No writable provider enabled (order: ${config.providers.order})")
            logger.info(
                "[CONFIG WRITE] Saving enrolment protocol body for project {} -> provider '{}' (order: {})",
                projectName,
                writable.id,
                config.providers.order,
            )
            writable.putEnrolmentProtocolBody(projectName, body)
            logger.info("[CONFIG WRITE] Enrolment protocol body saved for project {} via provider '{}'", projectName, writable.id)
        }
    }
