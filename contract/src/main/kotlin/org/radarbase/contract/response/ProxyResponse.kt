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

package org.radarbase.contract.response

/**
 * Encapsulates a raw HTTP response from an upstream microservice.
 *
 * The delegate layer uses this to faithfully proxy status codes,
 * content types, and body bytes without interpretation loss.
 */
data class ProxyResponse(
    val status: Int,
    val contentType: String? = null,
    val location: String? = null,
    val body: ByteArray? = null,
) {
    /** Decode body bytes to a UTF-8 string, or empty string if null. */
    fun bodyAsString(): String = body?.decodeToString().orEmpty()

    /** Whether the upstream returned a 2xx status code. */
    val isSuccess: Boolean get() = status in 200..299

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProxyResponse) return false
        return status == other.status &&
            contentType == other.contentType &&
            location == other.location &&
            body.contentEquals(other.body)
    }

    override fun hashCode(): Int {
        var result = status
        result = 31 * result + (contentType?.hashCode() ?: 0)
        result = 31 * result + (location?.hashCode() ?: 0)
        result = 31 * result + (body?.contentHashCode() ?: 0)
        return result
    }
}

