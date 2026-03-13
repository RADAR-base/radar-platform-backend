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

package org.radarbase.core.model.user

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Shared serializable User model.
 *
 * Originated from user-service; used by delegate-api via the contract layer
 * to deserialize upstream user responses.
 */
@Serializable
data class User(
    val id: String,
    val projectId: String,
    val name: String,
    val email: String,
    val status: String,
    val metadata: Map<String, JsonElement> = emptyMap(),
)
