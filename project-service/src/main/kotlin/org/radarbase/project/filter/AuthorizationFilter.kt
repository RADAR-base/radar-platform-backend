package org.radarbase.project.filter

import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import org.slf4j.LoggerFactory

@Provider
class AuthorizationFilter : ContainerRequestFilter {
    private val logger = LoggerFactory.getLogger(AuthorizationFilter::class.java)

    override fun filter(requestContext: ContainerRequestContext) {
        val path = requestContext.uriInfo.path
        if (path.startsWith("health")) {
            return
        }

        val authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Service token missing or invalid for path: $path")
            requestContext.abortWith(
                Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Service token is required")
                    .build(),
            )
        }
    }
}
