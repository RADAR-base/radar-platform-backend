package org.radarbase.gateway.model

/**
 * Raw HTTP response captured from the upstream RADAR-Gateway.
 */
data class GatewayProxyResponse(
    val status: Int,
    val contentType: String? = null,
    val location: String? = null,
    val body: ByteArray? = null,
) {
    /** Whether the upstream returned a 2xx status code. */
    val isSuccess: Boolean get() = status in 200..299

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GatewayProxyResponse) return false
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
