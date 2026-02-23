package org.radarbase.config.provider

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import org.radarbase.config.config.ConfigServiceConfig
import org.radarbase.config.model.StudyConfig
import org.radarbase.config.model.StudyDefinition
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * DB-backed provider. When enabled, stores study config, protocol body, and questionnaire
 * bodies in memory. Replace with a real database (e.g. PostgreSQL) for persistence.
 */
@Singleton
class DbStudyConfigProvider
    @Inject
    constructor(
        private val config: ConfigServiceConfig,
    ) : StudyConfigProvider {
        override val id: String = "db"

        override fun supportsWrites(): Boolean = true

        private val logger = LoggerFactory.getLogger(DbStudyConfigProvider::class.java)

        private val json =
            Json {
                ignoreUnknownKeys = true
            }

        private val studyDefinitions = ConcurrentHashMap<String, StudyDefinition>()
        private val protocolBodies = ConcurrentHashMap<String, String>()
        private val enrolmentLandingBodies = ConcurrentHashMap<String, String>()
        private val enrolmentProtocolBodies = ConcurrentHashMap<String, String>()
        private val questionnaireBodies = ConcurrentHashMap<String, String>()

        override suspend fun getStudyConfig(projectName: String): StudyConfig? {
            val dbConfig = config.providers.db
            if (!dbConfig.enabled) {
                return null
            }

            val definition = studyDefinitions[projectName] ?: return null
            logger.debug("Study config for project {} resolved from db provider", projectName)
            return StudyConfig(projectName = projectName, source = id, config = definition)
        }

        override suspend fun saveStudyDefinition(
            projectName: String,
            definition: StudyDefinition,
        ) {
            studyDefinitions[projectName] = definition
            logger.info("Study definition saved for project {} via db provider", projectName)
        }

        override fun getProtocolBody(projectName: String): String? = protocolBodies[projectName]

        override fun putProtocolBody(
            projectName: String,
            body: String,
        ) {
            protocolBodies[projectName] = body
            logger.info("Protocol body saved for project {} via db provider", projectName)
        }

        override fun getEnrolmentLandingBody(projectName: String): String? = enrolmentLandingBodies[projectName]

        override fun putEnrolmentLandingBody(
            projectName: String,
            body: String,
        ) {
            enrolmentLandingBodies[projectName] = body
            logger.info("Enrolment landing body saved for project {} via db provider", projectName)
        }

        override fun getEnrolmentProtocolBody(projectName: String): String? = enrolmentProtocolBodies[projectName]

        override fun putEnrolmentProtocolBody(
            projectName: String,
            body: String,
        ) {
            enrolmentProtocolBodies[projectName] = body
            logger.info("Enrolment protocol body saved for project {} via db provider", projectName)
        }

        override fun getQuestionnaireBody(
            projectName: String,
            questionnaireId: String,
        ): String? = questionnaireBodies[key(projectName, questionnaireId)]

        override fun putQuestionnaireBody(
            projectName: String,
            questionnaireId: String,
            body: String,
        ) {
            questionnaireBodies[key(projectName, questionnaireId)] = body
            logger.info("Questionnaire body saved for {} / {} via db provider", projectName, questionnaireId)
        }

        private fun key(
            projectName: String,
            questionnaireId: String,
        ): String = "$projectName|$questionnaireId"
    }
