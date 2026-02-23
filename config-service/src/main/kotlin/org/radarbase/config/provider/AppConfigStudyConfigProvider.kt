package org.radarbase.config.provider

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.radarbase.config.config.ConfigServiceConfig
import org.radarbase.config.model.StudyConfig
import org.radarbase.config.model.StudyDefinition
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

@Singleton
class AppConfigStudyConfigProvider
    @Inject
    constructor(
        private val config: ConfigServiceConfig,
    ) : StudyConfigProvider {
        override val id: String = "appConfig"

        override fun supportsWrites(): Boolean = true

        private val logger = LoggerFactory.getLogger(AppConfigStudyConfigProvider::class.java)

        private val json =
            Json {
                ignoreUnknownKeys = true
            }

        override suspend fun getStudyConfig(projectName: String): StudyConfig? {
            val appConfig = config.providers.appConfig
            if (!appConfig.enabled) {
                return null
            }

            val path: Path = projectPath(projectName).resolve("study-config.json")

            return try {
                if (!Files.exists(path)) {
                    logger.warn("Study config file not found for project $projectName at $path")
                    return null
                }

                val content = Files.readString(path)
                val definition = json.decodeFromString<StudyDefinition>(content)

                StudyConfig(
                    projectName = projectName,
                    source = id,
                    config = definition,
                )
            } catch (ex: Exception) {
                logger.error("Failed to read study config for project $projectName from $path", ex)
                null
            }
        }

        override suspend fun saveStudyDefinition(
            projectName: String,
            definition: StudyDefinition,
        ) {
            val appConfig = config.providers.appConfig
            if (!appConfig.enabled) return

            val dir = projectPath(projectName)
            Files.createDirectories(dir)
            val path = dir.resolve("study-config.json")
            Files.writeString(path, json.encodeToString(definition))
            logger.info("Study definition saved for project {} via appConfig provider at {}", projectName, path)
        }

        override fun getProtocolBody(projectName: String): String? {
            val appConfig = config.providers.appConfig
            if (!appConfig.enabled) return null

            val path = projectPath(projectName).resolve("protocol.json")
            return if (Files.exists(path)) {
                try {
                    Files.readString(path)
                } catch (ex: Exception) {
                    logger.warn("Failed to read protocol body for project {} from {}", projectName, path, ex)
                    null
                }
            } else {
                null
            }
        }

        override fun putProtocolBody(
            projectName: String,
            body: String,
        ) {
            val appConfig = config.providers.appConfig
            if (!appConfig.enabled) return

            val dir = projectPath(projectName)
            Files.createDirectories(dir)
            val path = dir.resolve("protocol.json")
            Files.writeString(path, body)
            logger.info("Protocol body saved for project {} via appConfig provider at {}", projectName, path)
        }

        override fun getEnrolmentLandingBody(projectName: String): String? {
            val appConfig = config.providers.appConfig
            if (!appConfig.enabled) return null

            val path = projectPath(projectName).resolve("enrolment").resolve("landingpage.json")
            return if (Files.exists(path)) {
                try {
                    Files.readString(path)
                } catch (ex: Exception) {
                    logger.warn("Failed to read enrolment landing body for project {} from {}", projectName, path, ex)
                    null
                }
            } else {
                null
            }
        }

        override fun putEnrolmentLandingBody(
            projectName: String,
            body: String,
        ) {
            val appConfig = config.providers.appConfig
            if (!appConfig.enabled) return

            val dir = projectPath(projectName).resolve("enrolment")
            Files.createDirectories(dir)
            val path = dir.resolve("landingpage.json")
            Files.writeString(path, body)
            logger.info("Enrolment landing body saved for project {} via appConfig provider at {}", projectName, path)
        }

        override fun getEnrolmentProtocolBody(projectName: String): String? {
            val appConfig = config.providers.appConfig
            if (!appConfig.enabled) return null

            val path = projectPath(projectName).resolve("enrolment").resolve("protocol.json")
            return if (Files.exists(path)) {
                try {
                    Files.readString(path)
                } catch (ex: Exception) {
                    logger.warn("Failed to read enrolment protocol body for project {} from {}", projectName, path, ex)
                    null
                }
            } else {
                null
            }
        }

        override fun putEnrolmentProtocolBody(
            projectName: String,
            body: String,
        ) {
            val appConfig = config.providers.appConfig
            if (!appConfig.enabled) return

            val dir = projectPath(projectName).resolve("enrolment")
            Files.createDirectories(dir)
            val path = dir.resolve("protocol.json")
            Files.writeString(path, body)
            logger.info("Enrolment protocol body saved for project {} via appConfig provider at {}", projectName, path)
        }

        override fun getQuestionnaireBody(
            projectName: String,
            questionnaireId: String,
        ): String? {
            val appConfig = config.providers.appConfig
            if (!appConfig.enabled) return null

            val path = projectPath(projectName).resolve("questionnaires").resolve("$questionnaireId.json")
            return if (Files.exists(path)) {
                try {
                    Files.readString(path)
                } catch (ex: Exception) {
                    logger.warn("Failed to read questionnaire body for {} / {} from {}", projectName, questionnaireId, path, ex)
                    null
                }
            } else {
                null
            }
        }

        override fun putQuestionnaireBody(
            projectName: String,
            questionnaireId: String,
            body: String,
        ) {
            val appConfig = config.providers.appConfig
            if (!appConfig.enabled) return

            val dir = projectPath(projectName).resolve("questionnaires")
            Files.createDirectories(dir)
            val path = dir.resolve("$questionnaireId.json")
            Files.writeString(path, body)
            logger.info("Questionnaire body saved for {} / {} via appConfig provider at {}", projectName, questionnaireId, path)
        }

        private fun projectPath(projectName: String): Path = Path.of(config.providers.appConfig.basePath, projectName)
    }
