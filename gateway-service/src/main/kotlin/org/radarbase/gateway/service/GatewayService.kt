package org.radarbase.gateway.service

import org.radarbase.gateway.model.GatewayProxyResponse

interface GatewayService {
    suspend fun listTopics(authToken: String?): GatewayProxyResponse

    suspend fun getTopic(
        topic: String,
        authToken: String?,
    ): GatewayProxyResponse

    suspend fun produceToTopic(
        topic: String,
        contentType: String?,
        body: ByteArray,
        authToken: String?,
    ): GatewayProxyResponse

    suspend fun uploadFile(
        projectId: String,
        subjectId: String,
        topic: String,
        fileName: String,
        content: ByteArray,
        authToken: String?,
    ): GatewayProxyResponse
}
