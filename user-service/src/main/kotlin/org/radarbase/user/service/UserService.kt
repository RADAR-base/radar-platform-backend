package org.radarbase.user.service

import org.radarbase.user.model.CreateParticipantRequest
import org.radarbase.user.model.KratosUser
import org.radarbase.user.model.ManagementPortalProxyResponse
import org.radarbase.user.model.RadarUser
import org.radarbase.user.model.TriggerRecoveryEmailRequest
import org.radarbase.user.model.UpdateUserRequest
import org.radarbase.user.model.UserDTO

interface UserService {
    /**
     * Get all users with information from Kratos and Radar
     * @param projectId The Radar project ID
     * @param authToken The authorization token for Radar API
     * @return List of user information
     */
    suspend fun getUsers(
        projectId: String,
        authToken: String,
    ): List<CombinedUser>

    /**
     * Get a specific user with information from Kratos and Radar
     * @param projectId The Radar project ID
     * @param userId The user ID
     * @param authToken The authorization token for Radar API
     * @return User information or null if not found
     */
    suspend fun getUser(
        projectId: String,
        userId: String,
        authToken: String,
    ): CombinedUser?

    /**
     * Get a user by their email address
     * @param email The user's email address
     * @return User information or null if not found
     */
    suspend fun getUserByEmail(
        projectId: String,
        email: String,
        authToken: String,
    ): CombinedUser?

    /**
     * Get all participants with information from Kratos and Radar
     * @param projectId The Radar project ID
     * @param authToken The authorization token for Radar API
     * @return List of participant information
     */
    suspend fun getParticipants(
        projectId: String,
        authToken: String,
    ): List<CombinedUser>

    /**
     * Get a specific participant with information from Kratos and Radar
     * @param projectId The Radar project ID
     * @param participantId The participant ID
     * @param authToken The authorization token for Radar API
     * @return Participant information or null if not found
     */
    suspend fun getParticipant(
        projectId: String,
        participantId: String,
        authToken: String,
    ): CombinedUser?

    /**
     * Get a participant by their email address
     * @param projectId The Radar project ID
     * @param email The participant's email address
     * @param authToken The authorization token for Radar API
     * @return Participant information or null if not found
     */
    suspend fun getParticipantByEmail(
        projectId: String,
        email: String,
        authToken: String,
    ): CombinedUser?

    /**
     * Update a user with information propagated to both Kratos and Radar
     * @param projectId The Radar project ID
     * @param userId The user ID
     * @param updateRequest The update request containing fields to update
     * @param authToken The authorization token for Radar API
     * @return Updated user information or null if update failed
     */
    suspend fun updateUser(
        projectId: String,
        userId: String,
        updateRequest: UpdateUserRequest,
        authToken: String,
    ): CombinedUser?

    /**
     * Update a participant with information propagated to both Kratos and Radar
     * @param projectId The Radar project ID
     * @param participantId The participant ID
     * @param updateRequest The update request containing fields to update
     * @param authToken The authorization token for Radar API
     * @return Updated participant information or null if update failed
     */
    suspend fun updateParticipant(
        projectId: String,
        participantId: String,
        updateRequest: UpdateUserRequest,
        authToken: String,
    ): CombinedUser?

    /**
     * Create a participant by creating a Kratos identity and then notifying Management Portal
     * through its Kratos subjects webhook endpoint.
     */
    suspend fun createParticipant(
        projectId: String,
        request: CreateParticipantRequest,
        authToken: String,
    ): CombinedUser?

    /**
     * Create a researcher/admin user in Management Portal (which will create the Kratos identity).
     * Request body matches MP's UserDTO.
     */
    suspend fun createUser(
        projectId: String,
        request: UserDTO,
        authToken: String,
    ): ManagementPortalProxyResponse

    /**
     * Trigger a Kratos recovery email for a user identity.
     */
    suspend fun triggerRecoveryEmail(
        request: TriggerRecoveryEmailRequest,
        authToken: String,
    ): Boolean
}

/**
 * Data class representing user information from both Kratos and Radar
 */
data class CombinedUser(
    val id: String,
    val projectId: String,
    val email: String,
    val radarMpId: String?,
    val radarInfo: RadarUser?,
    val kratosInfo: KratosUser?,
)
