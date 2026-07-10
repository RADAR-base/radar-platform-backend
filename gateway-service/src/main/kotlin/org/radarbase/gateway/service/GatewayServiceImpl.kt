package org.radarbase.gateway.service

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.radarbase.gateway.client.RadarGatewayClient
import org.radarbase.gateway.model.GatewayProxyResponse

@Singleton
class GatewayServiceImpl
@Inject constructor(
    private val radarGatewayClient: RadarGatewayClient,
) : GatewayService {
    override suspend fun listTopics(authToken: String?): GatewayProxyResponse =
        radarGatewayClient.listTopics(authToken)

    override suspend fun getTopic(
        topic: String,
        authToken: String?,
    ): GatewayProxyResponse = radarGatewayClient.getTopic(topic, authToken)

    override suspend fun produceToTopic(
        topic: String,
        contentType: String?,
        body: ByteArray,
        authToken: String?,
    ): GatewayProxyResponse = radarGatewayClient.produceToTopic(topic, contentType, body, authToken)

    override suspend fun uploadFile(
        projectId: String,
        subjectId: String,
        topic: String,
        fileName: String,
        content: ByteArray,
        authToken: String?,
    ): GatewayProxyResponse = radarGatewayClient.uploadFile(projectId, subjectId, topic, fileName, content, authToken)
}
