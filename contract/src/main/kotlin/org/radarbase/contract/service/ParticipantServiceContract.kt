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
 * Contract for all calls to the **user-service** participant endpoints.
 */
object ParticipantServiceContract {
    private const val SERVICE_NAME = "participant-service"
    private val client get() = ClientsContract.retrieveClientForService(SERVICE_NAME)

    suspend fun getParticipants(baseUrl: String, projectId: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::getParticipants") {
            client.get("$baseUrl/project/$projectId/participants") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.let { createProxyFromResponse(it) }
        }

    suspend fun getParticipant(
        baseUrl: String,
        projectId: String,
        participantId: String,
        authToken: String?,
    ): ProxyResponse {
        return tryProxyRequest("$SERVICE_NAME::getParticipant") {
            client.get("$baseUrl/project/$projectId/participants/$participantId") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }.let { createProxyFromResponse(it) }
        }
    }

    suspend fun createParticipant(baseUrl: String, projectId: String, body: String, authToken: String?): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::createParticipant") {
            client.post("$baseUrl/project/$projectId/participants") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }.let { createProxyFromResponse(it) }
        }

    suspend fun updateParticipant(
        baseUrl: String,
        projectId: String,
        participantId: String,
        body: String,
        authToken: String?,
    ): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::updateParticipant") {
            client.put("$baseUrl/project/$projectId/participants/$participantId") {
                authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }.let { createProxyFromResponse(it) }
        }
}

