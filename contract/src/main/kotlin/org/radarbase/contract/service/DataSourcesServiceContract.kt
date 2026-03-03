/*
 * Copyright 2025 King's College London
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
import org.radarbase.contract.client.ClientsContract
import org.radarbase.contract.response.ProxyResponse
import org.radarbase.contract.utils.ContractUtils.createProxyFromResponse
import org.radarbase.contract.utils.ContractUtils.tryProxyRequest

/**
 * Contract for all calls to the **data-sources-service** microservice.
 */
object DataSourcesServiceContract {
    private const val SERVICE_NAME = "data-sources-service"
    private val client get() = ClientsContract.retrieveClientForService(SERVICE_NAME)

    suspend fun getSourceCatalog(baseUrl: String): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::getSourceCatalog") {
            client.get("$baseUrl/sources")
                .let { createProxyFromResponse(it) }
        }

    suspend fun getParticipantSources(
        baseUrl: String,
        projectId: String,
        participantId: String,
    ): ProxyResponse =
        tryProxyRequest("$SERVICE_NAME::getParticipantSources") {
            client.get("$baseUrl/projects/$projectId/participants/$participantId/sources")
                .let { createProxyFromResponse(it) }
        }
}

