/*
 * Copyright 2026 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.contract.service

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.radarbase.contract.client.ClientsContract
import org.radarbase.contract.response.ProxyResponse
import org.radarbase.contract.utils.ContractUtils.createProxyFromResponse
import org.radarbase.contract.utils.ContractUtils.tryProxyRequest

/**
 * Contract for all calls to the **config-service** microservice.
 *
 * Every method returns a [ProxyResponse], callers can either
 * forward the raw proxy or use [deserializeDtoFromContract][org.radarbase.contract.utils.ContractUtils.deserializeDtoFromContract]
 * to get a typed DTO.
 */
object ConfigServiceContract {
    private const val SERVICE_NAME = "config-service"
    private val client get() = ClientsContract.retrieveClientForService(SERVICE_NAME)

    suspend fun getStudyConfig(baseUrl: String, projectId: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::getStudyConfig") {
            client.get("$baseUrl/projects/$projectId") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.let { createProxyFromResponse(it) }
        }

    suspend fun getProtocol(baseUrl: String, projectId: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::getProtocol") {
            client.get("$baseUrl/projects/$projectId/sources/protocol") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.let { createProxyFromResponse(it) }
        }

    suspend fun getEnrolmentSource(baseUrl: String, projectId: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::getEnrolmentSource") {
            client.get("$baseUrl/projects/$projectId/sources/enrolment") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.let { createProxyFromResponse(it) }
        }

    suspend fun getEnrolmentLanding(baseUrl: String, projectId: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::getEnrolmentLanding") {
            client.get("$baseUrl/projects/$projectId/enrolment/landing") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.let { createProxyFromResponse(it) }
        }

    suspend fun getEnrolmentProtocol(baseUrl: String, projectId: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::getEnrolmentProtocol") {
            client.get("$baseUrl/projects/$projectId/enrolment/protocol") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.let { createProxyFromResponse(it) }
        }

    suspend fun getQuestionnaires(baseUrl: String, projectId: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::getQuestionnaires") {
            client.get("$baseUrl/projects/$projectId/questionnaires") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.let { createProxyFromResponse(it) }
        }

    suspend fun getQuestionnaire(
        baseUrl: String,
        projectId: String,
        questionnaireId: String,
        authToken: String?,
    ): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::getQuestionnaire") {
            client.get("$baseUrl/projects/$projectId/questionnaires/$questionnaireId") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.let { createProxyFromResponse(it) }
        }

    suspend fun updateStudyConfig(baseUrl: String, projectId: String, body: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::updateStudyConfig") {
            client.put("$baseUrl/projects/$projectId") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }.let { createProxyFromResponse(it) }
        }

    suspend fun updateQuestionnaires(baseUrl: String, projectId: String, body: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::updateQuestionnaires") {
            client.put("$baseUrl/projects/$projectId/questionnaires") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }.let { createProxyFromResponse(it) }
        }

    suspend fun createQuestionnaire(baseUrl: String, projectId: String, body: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::createQuestionnaire") {
            client.post("$baseUrl/projects/$projectId/questionnaires") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }.let { createProxyFromResponse(it) }
        }

    suspend fun updateQuestionnaire(
        baseUrl: String,
        projectId: String,
        questionnaireId: String,
        body: String,
        authToken: String?,
    ): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::updateQuestionnaire") {
            client.put("$baseUrl/projects/$projectId/questionnaires/$questionnaireId") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }.let { createProxyFromResponse(it) }
        }

    suspend fun deleteQuestionnaire(baseUrl: String, projectId: String, questionnaireId: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::deleteQuestionnaire") {
            client.delete("$baseUrl/projects/$projectId/questionnaires/$questionnaireId") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.let { createProxyFromResponse(it) }
        }

    suspend fun updateProtocolSource(baseUrl: String, projectId: String, body: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::updateProtocolSource") {
            client.put("$baseUrl/projects/$projectId/sources/protocol") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }.let { createProxyFromResponse(it) }
        }

    suspend fun updateProtocolBody(baseUrl: String, projectId: String, body: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::updateProtocolBody") {
            client.put("$baseUrl/projects/$projectId/protocol") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }.let { createProxyFromResponse(it) }
        }

    suspend fun updateEnrolmentSource(baseUrl: String, projectId: String, body: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::updateEnrolmentSource") {
            client.put("$baseUrl/projects/$projectId/sources/enrolment") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }.let { createProxyFromResponse(it) }
        }

    suspend fun updateEnrolmentLandingBody(baseUrl: String, projectId: String, body: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::updateEnrolmentLandingBody") {
            client.put("$baseUrl/projects/$projectId/enrolment/landing") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }.let { createProxyFromResponse(it) }
        }

    suspend fun updateEnrolmentProtocolBody(baseUrl: String, projectId: String, body: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::updateEnrolmentProtocolBody") {
            client.put("$baseUrl/projects/$projectId/enrolment/protocol") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }.let { createProxyFromResponse(it) }
        }
}
