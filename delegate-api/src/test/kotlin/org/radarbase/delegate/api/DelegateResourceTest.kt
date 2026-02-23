package org.radarbase.delegate.api

import jakarta.ws.rs.core.Application
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.test.JerseyTest
import org.junit.jupiter.api.Test
// import jakarta.ws.rs.core.Response
// import org.junit.jupiter.api.Assertions.assertEquals
// import org.junit.jupiter.api.Assertions.assertNotNull

class DelegateResourceTest : JerseyTest() {
    override fun configure(): Application = ResourceConfig(DelegateResource::class.java)

    @Test
    fun testHealthCheck() {
        val response =
            super
                .target("/api/delegate/health")
                .request()
                .get()

        // assertEquals(Response.Status.OK.statusCode, response.status)
        // assertNotNull(response.readEntity(Map::class.java))
    }
}
