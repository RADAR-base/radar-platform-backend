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
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.radarbase.contract.client.ClientsContract
import org.radarbase.contract.response.ProxyResponse
import org.radarbase.contract.utils.ContractUtils.createProxyFromResponse
import org.radarbase.contract.utils.ContractUtils.tryProxyRequest

/**
 * Contract for all calls to the **project-service** microservice.
 */
object ProjectServiceContract {
    private const val SERVICE_NAME = "project-service"
    private val client get() = ClientsContract.retrieveClientForService(SERVICE_NAME)

    suspend fun getProjects(baseUrl: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::getProjects") {
            client.get("$baseUrl/projects") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.let { createProxyFromResponse(it) }
        }

    suspend fun getProject(baseUrl: String, projectId: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::getProject") {
            client.get("$baseUrl/projects/$projectId") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.let { createProxyFromResponse(it) }
        }

    suspend fun getProjectParticipants(baseUrl: String, projectId: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::getProjectParticipants") {
            client.get("$baseUrl/projects/$projectId/participants") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.let { createProxyFromResponse(it) }
        }

    suspend fun getProjectParticipant(
        baseUrl: String,
        projectId: String,
        participantId: String,
        authToken: String?,
    ): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::getProjectParticipant") {
            client.get("$baseUrl/projects/$projectId/participants/$participantId") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.let { createProxyFromResponse(it) }
        }

    suspend fun listGroups(baseUrl: String, projectName: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::listGroups") {
            client.get("$baseUrl/projects/$projectName/groups") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.let { createProxyFromResponse(it) }
        }

    suspend fun createGroup(baseUrl: String, projectName: String, body: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::createGroup") {
            client.post("$baseUrl/projects/$projectName/groups") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }.let { createProxyFromResponse(it) }
        }

    suspend fun deleteGroup(
        baseUrl: String,
        projectName: String,
        groupName: String,
        unlinkSubjects: Boolean,
        authToken: String?,
    ): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::deleteGroup") {
            val url = buildString {
                append("$baseUrl/projects/$projectName/groups/$groupName")
                if (unlinkSubjects) append("?unlinkSubjects=true")
            }
            client.delete(url) {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.let { createProxyFromResponse(it) }
        }
}

