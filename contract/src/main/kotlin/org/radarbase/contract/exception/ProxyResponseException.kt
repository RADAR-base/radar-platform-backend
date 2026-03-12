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

package org.radarbase.contract.exception

import jakarta.ws.rs.core.Response
import org.radarbase.jersey.exception.HttpApplicationException

/**
 * Thrown when an upstream microservice returns a non-2xx response.
 * The original status code is faithfully preserved so the delegate
 * can relay it to the caller.
 */
class ProxyResponseException(
    status: Response.Status,
    message: String,
    code: String = "request_failed",
) : HttpApplicationException(status, code, message)
