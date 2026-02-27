package org.radarbase.config.provider

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer
import org.radarbase.config.config.ConfigServiceConfig
import org.radarbase.config.model.StudyConfig
import org.radarbase.config.model.StudyDefinition
import org.slf4j.LoggerFactory
import java.util.Base64

@Singleton
class GithubStudyConfigProvider
@Inject
constructor(
    private val config: ConfigServiceConfig,
) : StudyConfigProvider {
    override fun supportsWrites(): Boolean = true

    override val id: String = "github"

    private val logger = LoggerFactory.getLogger(GithubStudyConfigProvider::class.java)

    private val client: HttpClient =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    private val jsonForWriting =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }

    private val jsonPut =
        Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
        }

    private val githubConfig get() = config.providers.github

    private fun contentsApiUrl(): String = "https://api.github.com/repos/${githubConfig.owner}/${githubConfig.repo}/contents"

    private fun rawUrl(path: String): String = "${githubConfig.baseUrl.trimEnd('/')}/$path"

    private fun questionnaireContentsApiUrl(): String =
        "https://api.github.com/repos/${githubConfig.questionnaireRepoOwner}/${githubConfig.questionnaireRepo}/contents"

    private fun questionnaireRawUrl(path: String): String =
        "https://raw.githubusercontent.com/${githubConfig.questionnaireRepoOwner}/" +
            "${githubConfig.questionnaireRepo}/${githubConfig.questionnaireRepoBranch}/$path"

    private fun useProtocolRepo(): Boolean =
        !githubConfig.protocolRepoOwner.isNullOrBlank() && !githubConfig.protocolRepo.isNullOrBlank()

    private fun protocolBranch(): String = githubConfig.protocolRepoBranch?.takeIf { it.isNotBlank() } ?: githubConfig.branch

    private fun protocolContentsApiUrl(): String =
        "https://api.github.com/repos/${githubConfig.protocolRepoOwner!!.trim()}/${githubConfig.protocolRepo!!.trim()}/contents"

    private fun protocolRawUrl(path: String): String =
        "https://raw.githubusercontent.com/${githubConfig.protocolRepoOwner!!.trim()}/" +
            "${githubConfig.protocolRepo!!.trim()}/${protocolBranch()}/$path"

    private fun useEnrolmentRepo(): Boolean =
        !githubConfig.enrolmentRepoOwner.isNullOrBlank() && !githubConfig.enrolmentRepo.isNullOrBlank()

    private fun enrolmentBranch(): String = githubConfig.enrolmentRepoBranch?.takeIf { it.isNotBlank() } ?: githubConfig.branch

    private fun enrolmentContentsApiUrl(): String =
        "https://api.github.com/repos/${githubConfig.enrolmentRepoOwner!!.trim()}/${githubConfig.enrolmentRepo!!.trim()}/contents"

    private suspend fun getEnrolmentFileSha(path: String): String? {
        val token = githubConfig.token ?: return null
        val url = "${enrolmentContentsApiUrl()}/$path?ref=${enrolmentBranch()}"
        return try {
            val response =
                client.get(url) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.Accept, "application/vnd.github+json")
                    header("X-GitHub-Api-Version", "2022-11-28")
                }
            if (response.status != HttpStatusCode.OK) return null
            val body = response.bodyAsText()
            val element = json.parseToJsonElement(body)
            element.jsonObject["sha"]?.jsonPrimitive?.content
        } catch (ex: Exception) {
            logger.warn("[GITHUB] GET enrolment repo {} failed: {}", path, ex.message)
            null
        }
    }

    private suspend fun getEnrolmentRepoFileContent(path: String): String? {
        val token = githubConfig.token ?: return null
        val url = "${enrolmentContentsApiUrl()}/$path?ref=${enrolmentBranch()}"
        return try {
            val response =
                client.get(url) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.Accept, "application/vnd.github+json")
                    header("X-GitHub-Api-Version", "2022-11-28")
                }
            if (response.status != HttpStatusCode.OK) return null
            val body = response.bodyAsText()
            val element = json.parseToJsonElement(body)
            val content = element.jsonObject["content"]?.jsonPrimitive?.content ?: return null
            String(Base64.getDecoder().decode(content.replace("\n", "")))
        } catch (ex: Exception) {
            logger.warn("[GITHUB] getEnrolmentRepoFileContent {} failed: {}", path, ex.message)
            null
        }
    }

    private suspend fun putEnrolmentRepoFile(
        path: String,
        content: String,
        message: String,
    ) {
        val token =
            githubConfig.token
                ?: throw IllegalStateException("GitHub token required for enrolment repo writes")
        val sha = getEnrolmentFileSha(path)
        val base64Content = Base64.getEncoder().encodeToString(content.toByteArray(Charsets.UTF_8))
        val url = "${enrolmentContentsApiUrl()}/$path"
        val putBody =
            GithubPutContentRequest(
                message = message,
                content = base64Content,
                sha = sha,
                branch = enrolmentBranch(),
            )
        val bodyString = jsonPut.encodeToString(serializer<GithubPutContentRequest>(), putBody)
        logger.info(
            "[GITHUB] PUT enrolment repo {} branch={} message={}",
            url,
            enrolmentBranch(),
            message,
        )
        val response =
            client.put(url) {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, "application/vnd.github+json")
                header("X-GitHub-Api-Version", "2022-11-28")
                header(HttpHeaders.ContentType, "application/json")
                setBody(bodyString)
            }
        val responseBody = response.bodyAsText()
        if (response.status.value !in 200..299) {
            logger.error("[GITHUB] PUT enrolment repo {} failed: status={} body={}", url, response.status.value, responseBody)
            throw IllegalStateException("Failed to write enrolment to repo: ${response.status.value}")
        }
    }

    private suspend fun getFileSha(path: String): String? {
        val token = githubConfig.token ?: return null
        val url = "${contentsApiUrl()}/$path?ref=${githubConfig.branch}"
        return try {
            logger.info("[GITHUB] GET {} (branch={})", url, githubConfig.branch)
            val response =
                client.get(url) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.Accept, "application/vnd.github+json")
                    header("X-GitHub-Api-Version", "2022-11-28")
                }
            val body = response.bodyAsText()
            logger.info("[GITHUB] GET {} -> status={}, body={}", url, response.status.value, truncate(body, 200))
            if (response.status != HttpStatusCode.OK) return null
            val element = json.parseToJsonElement(body)
            val sha = element.jsonObject["sha"]?.jsonPrimitive?.content
            logger.info("[GITHUB] GET {} -> sha={}", path, sha ?: "null (new file)")
            sha
        } catch (ex: Exception) {
            logger.warn("[GITHUB] GET {} failed: {}", path, ex.message)
            null
        }
    }

    private suspend fun putFile(
        path: String,
        content: String,
        message: String,
    ) {
        val token =
            githubConfig.token
                ?: throw IllegalStateException("GitHub token required for writes")
        val sha = getFileSha(path)
        val base64Content = Base64.getEncoder().encodeToString(content.toByteArray(Charsets.UTF_8))
        val url = "${contentsApiUrl()}/$path"
        val putBody =
            GithubPutContentRequest(
                message = message,
                content = base64Content,
                sha = sha,
                branch = githubConfig.branch,
            )
        val bodyString = jsonPut.encodeToString(serializer<GithubPutContentRequest>(), putBody)
        logger.info(
            "[GITHUB] PUT {} branch={} message={} sha={} contentLen={}",
            url,
            githubConfig.branch,
            message,
            sha ?: "null (create)",
            content.length,
        )
        val response =
            client.put(url) {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, "application/vnd.github+json")
                header("X-GitHub-Api-Version", "2022-11-28")
                header(HttpHeaders.ContentType, "application/json")
                setBody(bodyString)
            }
        val responseBody = response.bodyAsText()
        logger.info(
            "[GITHUB] PUT {} -> status={}, body={}",
            url,
            response.status.value,
            truncate(responseBody, 500),
        )
        if (response.status.value !in 200..299) {
            logger.error("[GITHUB] PUT {} failed: status={} body={}", url, response.status.value, responseBody)
        }
    }

    private suspend fun getQuestionnaireFileSha(path: String): String? {
        val token = githubConfig.token ?: return null
        val url = "${questionnaireContentsApiUrl()}/$path?ref=${githubConfig.questionnaireRepoBranch}"
        return try {
            val response =
                client.get(url) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.Accept, "application/vnd.github+json")
                    header("X-GitHub-Api-Version", "2022-11-28")
                }
            if (response.status != HttpStatusCode.OK) return null
            val body = response.bodyAsText()
            val element = json.parseToJsonElement(body)
            element.jsonObject["sha"]?.jsonPrimitive?.content
        } catch (ex: Exception) {
            logger.warn("[GITHUB] GET questionnaire repo {} failed: {}", path, ex.message)
            null
        }
    }

    private suspend fun putQuestionnaireRepoFile(
        path: String,
        content: String,
        message: String,
    ) {
        val token =
            githubConfig.token
                ?: throw IllegalStateException("GitHub token required for questionnaire repo writes")
        val sha = getQuestionnaireFileSha(path)
        val base64Content = Base64.getEncoder().encodeToString(content.toByteArray(Charsets.UTF_8))
        val url = "${questionnaireContentsApiUrl()}/$path"
        val putBody =
            GithubPutContentRequest(
                message = message,
                content = base64Content,
                sha = sha,
                branch = githubConfig.questionnaireRepoBranch,
            )
        val bodyString = jsonPut.encodeToString(serializer<GithubPutContentRequest>(), putBody)
        logger.info(
            "[GITHUB] PUT questionnaire repo {} branch={} message={}",
            url,
            githubConfig.questionnaireRepoBranch,
            message,
        )
        val response =
            client.put(url) {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, "application/vnd.github+json")
                header("X-GitHub-Api-Version", "2022-11-28")
                header(HttpHeaders.ContentType, "application/json")
                setBody(bodyString)
            }
        val responseBody = response.bodyAsText()
        if (response.status.value !in 200..299) {
            logger.error("[GITHUB] PUT questionnaire repo {} failed: status={} body={}", url, response.status.value, responseBody)
            throw IllegalStateException("Failed to write questionnaire to repo: ${response.status.value}")
        }
    }

    private fun truncate(
        s: String,
        maxLen: Int,
    ): String = if (s.length <= maxLen) s else s.take(maxLen) + "... (${s.length} chars)"

    /** Fetch file content via Contents API (avoids raw CDN cache so reads see latest updates). */
    private suspend fun getFileContentViaApi(path: String): String? {
        val token = githubConfig.token ?: return null
        val url = "${contentsApiUrl()}/$path?ref=${githubConfig.branch}"
        return try {
            val response =
                client.get(url) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.Accept, "application/vnd.github+json")
                    header("X-GitHub-Api-Version", "2022-11-28")
                }
            if (response.status != HttpStatusCode.OK) return null
            val body = response.bodyAsText()
            val element = json.parseToJsonElement(body)
            val content = element.jsonObject["content"]?.jsonPrimitive?.content ?: return null
            String(Base64.getDecoder().decode(content.replace("\n", "")))
        } catch (ex: Exception) {
            logger.warn("[GITHUB] getFileContentViaApi {} failed: {}", path, ex.message)
            null
        }
    }

    private suspend fun getProtocolFileSha(path: String): String? {
        val token = githubConfig.token ?: return null
        val url = "${protocolContentsApiUrl()}/$path?ref=${protocolBranch()}"
        return try {
            val response =
                client.get(url) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.Accept, "application/vnd.github+json")
                    header("X-GitHub-Api-Version", "2022-11-28")
                }
            if (response.status != HttpStatusCode.OK) return null
            val body = response.bodyAsText()
            val element = json.parseToJsonElement(body)
            element.jsonObject["sha"]?.jsonPrimitive?.content
        } catch (ex: Exception) {
            logger.warn("[GITHUB] GET protocol repo {} failed: {}", path, ex.message)
            null
        }
    }

    private suspend fun getProtocolRepoFileContent(path: String): String? {
        val token = githubConfig.token ?: return null
        val url = "${protocolContentsApiUrl()}/$path?ref=${protocolBranch()}"
        return try {
            val response =
                client.get(url) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.Accept, "application/vnd.github+json")
                    header("X-GitHub-Api-Version", "2022-11-28")
                }
            if (response.status != HttpStatusCode.OK) return null
            val body = response.bodyAsText()
            val element = json.parseToJsonElement(body)
            val content = element.jsonObject["content"]?.jsonPrimitive?.content ?: return null
            String(Base64.getDecoder().decode(content.replace("\n", "")))
        } catch (ex: Exception) {
            logger.warn("[GITHUB] getProtocolRepoFileContent {} failed: {}", path, ex.message)
            null
        }
    }

    private suspend fun putProtocolRepoFile(
        path: String,
        content: String,
        message: String,
    ) {
        val token =
            githubConfig.token
                ?: throw IllegalStateException("GitHub token required for protocol repo writes")
        val sha = getProtocolFileSha(path)
        val base64Content = Base64.getEncoder().encodeToString(content.toByteArray(Charsets.UTF_8))
        val url = "${protocolContentsApiUrl()}/$path"
        val putBody =
            GithubPutContentRequest(
                message = message,
                content = base64Content,
                sha = sha,
                branch = protocolBranch(),
            )
        val bodyString = jsonPut.encodeToString(serializer<GithubPutContentRequest>(), putBody)
        logger.info(
            "[GITHUB] PUT protocol repo {} branch={} message={}",
            url,
            protocolBranch(),
            message,
        )
        val response =
            client.put(url) {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.Accept, "application/vnd.github+json")
                header("X-GitHub-Api-Version", "2022-11-28")
                header(HttpHeaders.ContentType, "application/json")
                setBody(bodyString)
            }
        val responseBody = response.bodyAsText()
        if (response.status.value !in 200..299) {
            logger.error("[GITHUB] PUT protocol repo {} failed: status={} body={}", url, response.status.value, responseBody)
            throw IllegalStateException("Failed to write protocol to repo: ${response.status.value}")
        }
    }

    private suspend fun getQuestionnaireRepoFileContent(path: String): String? {
        val token = githubConfig.token ?: return null
        val url = "${questionnaireContentsApiUrl()}/$path?ref=${githubConfig.questionnaireRepoBranch}"
        return try {
            val response =
                client.get(url) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    header(HttpHeaders.Accept, "application/vnd.github+json")
                    header("X-GitHub-Api-Version", "2022-11-28")
                }
            if (response.status != HttpStatusCode.OK) return null
            val body = response.bodyAsText()
            val element = json.parseToJsonElement(body)
            val content = element.jsonObject["content"]?.jsonPrimitive?.content ?: return null
            String(Base64.getDecoder().decode(content.replace("\n", "")))
        } catch (ex: Exception) {
            logger.warn("[GITHUB] getQuestionnaireRepoFileContent {} failed: {}", path, ex.message)
            null
        }
    }

    private fun prettyPrintJsonIfValid(content: String): String =
        try {
            val element = json.parseToJsonElement(content)
            jsonForWriting.encodeToString(
                kotlinx.serialization.json.JsonElement
                    .serializer(),
                element,
            )
        } catch (ex: Exception) {
            content
        }

    override suspend fun getStudyConfig(projectName: String): StudyConfig? {
        if (!githubConfig.enabled) return null

        val path = "$projectName/study-config.json"
        return try {
            logger.info("Fetching study config for project $projectName from GitHub (Contents API)")
            val body = getFileContentViaApi(path) ?: return null
            val element: JsonElement = json.parseToJsonElement(body)
            StudyConfig(
                projectName = projectName,
                source = id,
                config = json.decodeFromString<StudyDefinition>(element.toString()),
            )
        } catch (ex: Exception) {
            logger.error("Failed to fetch study config for project $projectName from GitHub", ex)
            null
        }
    }

    override suspend fun saveStudyDefinition(
        projectName: String,
        definition: StudyDefinition,
    ) {
        val path = "$projectName/study-config.json"
        val content = jsonForWriting.encodeToString(serializer<StudyDefinition>(), definition)
        putFile(path, content, "Update study config for $projectName")
        logger.info("Study definition saved for project {} via github provider", projectName)
    }

    override fun getProtocolBody(projectName: String): String? {
        if (!githubConfig.enabled) return null
        return runBlocking {
            try {
                val path = "$projectName/protocol.json"
                if (useProtocolRepo()) {
                    getProtocolRepoFileContent(path)
                } else {
                    getFileContentViaApi(path)
                }
            } catch (ex: Exception) {
                logger.debug("No protocol body for project {}: {}", projectName, ex.message)
                null
            }
        }
    }

    override fun putProtocolBody(
        projectName: String,
        body: String,
    ) {
        runBlocking {
            val path = "$projectName/protocol.json"
            val content = prettyPrintJsonIfValid(body)
            if (useProtocolRepo()) {
                putProtocolRepoFile(path, content, "Update protocol for $projectName")
                logger.info("Protocol body saved for project {} via github provider (protocol repo)", projectName)
            } else {
                putFile(path, content, "Update protocol for $projectName")
                logger.info("Protocol body saved for project {} via github provider", projectName)
            }
        }
    }

    override fun getEnrolmentLandingBody(projectName: String): String? {
        if (!githubConfig.enabled || !useEnrolmentRepo()) return null
        return runBlocking {
            try {
                val path = "$projectName/landingpage.json"
                getEnrolmentRepoFileContent(path)
            } catch (ex: Exception) {
                logger.debug("No enrolment landing body for project {}: {}", projectName, ex.message)
                null
            }
        }
    }

    override fun putEnrolmentLandingBody(
        projectName: String,
        body: String,
    ) {
        runBlocking {
            val path = "projects/$projectName/landingpage.json"
            val content = prettyPrintJsonIfValid(body)
            if (useEnrolmentRepo()) {
                putEnrolmentRepoFile(path, content, "Update enrolment landing for $projectName")
                logger.info("Enrolment landing body saved for project {} via github provider (enrolment repo)", projectName)
            } else {
                throw UnsupportedOperationException(
                    "Provider $id: enrolment repo not configured (set enrolmentRepoOwner/enrolmentRepo)",
                )
            }
        }
    }

    override fun getEnrolmentProtocolBody(projectName: String): String? {
        if (!githubConfig.enabled || !useEnrolmentRepo()) return null
        return runBlocking {
            try {
                val path = "$projectName/protocol.json"
                getEnrolmentRepoFileContent(path)
            } catch (ex: Exception) {
                logger.debug("No enrolment protocol body for project {}: {}", projectName, ex.message)
                null
            }
        }
    }

    override fun putEnrolmentProtocolBody(
        projectName: String,
        body: String,
    ) {
        runBlocking {
            val path = "projects/$projectName/protocol.json"
            val content = prettyPrintJsonIfValid(body)
            if (useEnrolmentRepo()) {
                putEnrolmentRepoFile(path, content, "Update enrolment protocol for $projectName")
                logger.info("Enrolment protocol body saved for project {} via github provider (enrolment repo)", projectName)
            } else {
                throw UnsupportedOperationException(
                    "Provider $id: enrolment repo not configured (set enrolmentRepoOwner/enrolmentRepo)",
                )
            }
        }
    }

    override fun getQuestionnaireBodyHref(
        projectName: String,
        questionnaireId: String,
    ): String? = questionnaireRawUrl("questionnaires/$questionnaireId/${questionnaireId}_armt.json")

    override fun putQuestionnaireBodyOnly(
        projectName: String,
        questionnaireId: String,
        body: String,
    ) {
        runBlocking {
            val path = "questionnaires/$questionnaireId/${questionnaireId}_armt.json"
            putQuestionnaireRepoFile(path, prettyPrintJsonIfValid(body), "Add/update questionnaire $questionnaireId")
        }
    }

    override fun getQuestionnaireBody(
        projectName: String,
        questionnaireId: String,
    ): String? {
        if (!githubConfig.enabled) return null
        return runBlocking {
            try {
                val path = "questionnaires/$questionnaireId/${questionnaireId}_armt.json"
                getQuestionnaireRepoFileContent(path)
            } catch (ex: Exception) {
                logger.debug("No questionnaire body for {} / {}: {}", projectName, questionnaireId, ex.message)
                null
            }
        }
    }

    override fun putQuestionnaireBody(
        projectName: String,
        questionnaireId: String,
        body: String,
    ) {
        runBlocking {
            val path = "questionnaires/$questionnaireId/${questionnaireId}_armt.json"
            putQuestionnaireRepoFile(path, prettyPrintJsonIfValid(body), "Add/update questionnaire $questionnaireId")
            val href = questionnaireRawUrl(path)
            val current =
                getStudyConfig(projectName)?.config
                    ?: throw NoSuchElementException("Study config not found for project $projectName")
            val updatedQuestionnaires =
                current.questionnaires.map { q ->
                    if (q.id == questionnaireId) q.copy(href = href) else q
                }
            if (updatedQuestionnaires.any { it.id == questionnaireId }) {
                saveStudyDefinition(projectName, current.copy(questionnaires = updatedQuestionnaires))
                logger.info("Questionnaire body saved for {} / {} via github provider (href={})", projectName, questionnaireId, href)
            } else {
                logger.debug(
                    "Questionnaire {} not in study definition for {}; href not updated (may be create-in-progress)",
                    questionnaireId,
                    projectName,
                )
            }
        }
    }

    @Serializable
    private data class GithubPutContentRequest(
        val message: String,
        val content: String,
        val sha: String? = null,
        @SerialName("branch") val branch: String,
    )
}
