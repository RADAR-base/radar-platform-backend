package org.radarbase.datasources.api

import io.mockk.mockk
import jakarta.ws.rs.core.Application
import jakarta.ws.rs.core.Response
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.test.JerseyTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.radarbase.datasources.service.DataSourcesService

class DataSourceResourceTest : JerseyTest() {
    override fun configure(): Application {
        val dataSourcesService = mockk<DataSourcesService>(relaxed = true)

        return ResourceConfig(DataSourceResource::class.java)
            .register(
                object : AbstractBinder() {
                    override fun configure() {
                        bind(dataSourcesService).to(DataSourcesService::class.java)
                    }
                },
            )
    }

    @Test
    fun testHealthCheck() {
        val response =
            super
                .target("/data-sources-service/health")
                .request()
                .get()

        assertEquals(Response.Status.OK.statusCode, response.status)
        assertNotNull(response.readEntity(Map::class.java))
    }
}
